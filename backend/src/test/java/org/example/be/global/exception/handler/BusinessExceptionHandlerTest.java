package org.example.be.global.exception.handler;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;

// BusinessExceptionHandler 단위 테스트.
// 스프링 컨텍스트 없이 핸들러를 직접 호출해 '응답 변환'과 '로그 레벨 분기'만 검증한다.
// (어느 advice 가 이 핸들러를 고르는가 = @Order 순서는 ExceptionHandlerOrderIT 가 검증한다)
//
// 로그는 핸들러 로거(@Slf4j)에 ListAppender 를 붙여 실제 로그 이벤트를 모아서 확인한다.
// 콘솔 출력 문자열을 긁는 방식과 달리 레벨, 포맷된 메시지, 예외 객체 포함 여부를 구조적으로 단언할 수 있다.
@DisplayName("BusinessExceptionHandler 응답 변환·로그 레벨 단위 테스트")
class BusinessExceptionHandlerTest {

	private final BusinessExceptionHandler businessExceptionHandler = new BusinessExceptionHandler();

	// JUnit 은 테스트 메서드마다 인스턴스를 새로 만들므로, 이 appender 도 테스트마다 비어 있는 상태로 시작한다.
	private final ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
	private final Logger handlerLogger = (Logger)LoggerFactory.getLogger(BusinessExceptionHandler.class);

	@BeforeEach
	void attachLogAppender() {
		logAppender.start();
		handlerLogger.addAppender(logAppender);
	}

	@AfterEach
	void detachLogAppender() {
		handlerLogger.detachAppender(logAppender);
		logAppender.stop();
	}

	@Test
	@DisplayName("4xx 는 WARN 한 줄로 남기고 스택은 남기지 않는다")
	void clientError_logsWarnWithoutStackTrace() {
		BusinessException exception = new BusinessException(ErrorCode.LOGIN_FAILED,
			"비밀번호 불일치 - email: tester@example.com");

		ResponseEntity<CommonResponse<Void>> response = businessExceptionHandler.handleBusinessException(exception);

		assertThat(response.getStatusCode().value()).isEqualTo(401);
		CommonResponse<Void> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.isSuccess()).isFalse();
		assertThat(body.getStatus()).isEqualTo(401);
		assertThat(body.getCode()).isEqualTo("LOGIN_FAILED");
		assertThat(body.getMessage()).isEqualTo(ErrorCode.LOGIN_FAILED.getMessage());

		List<ILoggingEvent> events = logAppender.list;
		assertThat(events).hasSize(1);
		ILoggingEvent event = events.get(0);
		assertThat(event.getLevel()).isEqualTo(Level.WARN);
		// 로그 접두어는 운영 로그 검색 키다. 바뀌면 기존 검색이 조용히 끊기므로 글자 그대로 고정한다.
		assertThat(event.getFormattedMessage())
			.isEqualTo("[BusinessException] code=LOGIN_FAILED, detail=비밀번호 불일치 - email: tester@example.com");
		// 4xx 는 사용자 입력 문제라 예상된 흐름이다. 스택을 남기면 진짜 장애가 로그에 묻힌다.
		assertThat(event.getThrowableProxy()).isNull();
	}

	@Test
	@DisplayName("5xx 는 ERROR 와 스택을 남기고, cause 체인까지 로그에 포함한다")
	void serverError_logsErrorWithStackTraceAndCause() {
		IOException cause = new IOException("GCS 연결 타임아웃");
		BusinessException exception = new BusinessException(ErrorCode.GCS_UPLOAD_FAILED,
			"게시글 이미지 업로드 실패 - fileName: a.png", cause);

		ResponseEntity<CommonResponse<Void>> response = businessExceptionHandler.handleBusinessException(exception);

		assertThat(response.getStatusCode().value()).isEqualTo(500);
		CommonResponse<Void> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.getCode()).isEqualTo("GCS_UPLOAD_FAILED");

		List<ILoggingEvent> events = logAppender.list;
		assertThat(events).hasSize(1);
		ILoggingEvent event = events.get(0);
		assertThat(event.getLevel()).isEqualTo(Level.ERROR);
		assertThat(event.getFormattedMessage())
			.isEqualTo("[BusinessException] code=GCS_UPLOAD_FAILED, detail=게시글 이미지 업로드 실패 - fileName: a.png");

		// 로그 이벤트에 예외 객체가 실려 있어야 appender 가 스택을 출력한다.
		// (log.error 의 마지막 인자로 e 를 넘기지 않으면 여기가 null 이 된다)
		IThrowableProxy throwableProxy = event.getThrowableProxy();
		assertThat(throwableProxy).isNotNull();
		assertThat(throwableProxy.getClassName()).isEqualTo(BusinessException.class.getName());
		// cause 가 빠지면 '파일 업로드에 실패했습니다'만 남고 진짜 원인(타임아웃 등)을 추적할 수 없다.
		assertThat(throwableProxy.getCause()).isNotNull();
		assertThat(throwableProxy.getCause().getClassName()).isEqualTo(IOException.class.getName());
		assertThat(throwableProxy.getCause().getMessage()).isEqualTo("GCS 연결 타임아웃");
	}

	@Test
	@DisplayName("응답 message 에는 ErrorCode 고정 문구만 담고 debugMessage·cause 메시지는 담지 않는다")
	void responseMessage_excludesDebugMessageAndCause() {
		// 서버 내부 정보(SMTP 주소, Redis 연결 문자열)가 응답으로 새던 과거 사례(MailController)를 재현한 입력
		String debugMessage = "SMTP 연결 실패 - host: smtp.internal.example:587";
		BusinessException exception = new BusinessException(ErrorCode.MAIL_SEND_FAILED, debugMessage,
			new IllegalStateException("redis://10.0.0.5:6379 연결 거부"));

		ResponseEntity<CommonResponse<Void>> response = businessExceptionHandler.handleBusinessException(exception);

		CommonResponse<Void> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.getMessage())
			.isEqualTo(ErrorCode.MAIL_SEND_FAILED.getMessage())
			.doesNotContain("smtp.internal")
			.doesNotContain("redis://");
	}
}