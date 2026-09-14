package org.example.be.global.exception.handler;

import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 우리 코드가 throw 한 예외(BusinessException) 전담 advice.
 * 컨트롤러, 서비스에서 올라온 BusinessException을 CommonResponse 규격으로 변환한다.
 *
 * advice 조회 순서 (4개 파일 상단에 글자 그대로 동일하게 유지할 것)
 *
 *        @Order(0) SseExceptionHandler : SSE 클라이언트 이탈, 비동기 타임아웃
 *        @Order(10) BusinessExceptionHandler : 우리 코드가 throw 한 BusinessException
 *        @Order(20) RequestExceptionHandler : 스프링, 톰캣이 throw 한 프레임워크 예외
 *          @Order(LOWEST_PRECEDENCE) FallbackExceptionHandler : catch-all (Exception)
 *
 * advice 사이에서는 '가장 구체적인 핸들러'가 아니라 '@Order 선착순'으로 결정된다.
 * catch-all 을 가진 Fallback 이 앞에 오면 나머지 세 advice 가 통째로 무력화되어 모든 응답이 500이 된다.
 * 순서가 '필수'인 것은 Fallback 이 마지막이라는 점 하나뿐이다. (나머지 셋은 잡는 예외가 서로 달라 겹치지 않음)
 *
 * 응답에 나가는 문구와 로그에 남는 문구는 다르다
 * 응답 <- ErrorCode.getMessage() : 사용자에게 보여줄 고정 문구
 * 로그 <- e.getMessage()         : BusinessException 생성 시 넘긴 debugMessage
 *
 * 이 분리가 정보 노출을 막는다. 예를 들어 로그인 실패는 "없는 이메일"과 "비밀번호 불일치"가
 * 로그에서만 갈리고 응답은 완전히 같아서 (LOGIN_FAILED) 계정 열거 공격이 통하지 않는다.
 * 예외 메시지를 응답 바디에 그대로 담지 말 것 - SMTP 주소, Redis 연결 문자열 같은 서버 내부 정보가 그대로 샌다.
 * (실제로 MailController 가 그렇게 짜여 있었다)
 *
 * 모든 advice 의 핸들러는 3개의 인자 CommonResponse.error(status, code, message)를 쓴다.
 * code : 프론트가 분기에 쓰는 공개 계약이라, 일부 응답에만 실리면 프론트가 매번
 * "code 가 없을 수도 있다"고 방어해야 해서 계약으로서 가치가 떨어진다.
 * 2개 인자 오버로드는 code 를 모르는 호출부(필터 등)를 위해 남아 있는 것이지 advice 용이 아니다.
 * 둘 다 유효한 오버로드라 컴파일로는 걸러지지 않으니 리뷰 때 서로 눈으로 확인해줄 것.
 *
 * cause 체인 주의 - 스프링은 advice 마다, 예외 타입이 직접 매칭되지 않으면 getCause() 를 따라 내려가며 다시 찾는다.
 * 그래서 BusinessException 을 cause 로 품은 다른 예외도 Fallback(500) 보다 먼저 이 advice 에 잡힌다.
 * (advice 가 하나였을 때는 catch-all 이 모든 예외에 '직접' 매칭되어 이 탐색이 일어나지 않았다)
 */
@Slf4j
@Order(10)
@RestControllerAdvice
public class BusinessExceptionHandler {

	/**
	 * 비즈니스 예외 처리 - ErrorCode 에 정의된 모든 에러를 여기서 처리한다.
	 *
	 * 로그 레벨을 상태코드로 가른다.
	 *
	 * - 5xx : 우리 잘못이다. ERROR + stack trace 로 남겨야 원인(타임아웃, 인증 실패 등)을 추적할 수 있다.
	 * - 4xx : 사용자 입력 문제라 예상된 흐름이다. 스택을 남기면 진짜 장애가 로그에 묻힌다.
	 *
	 * cause 는 BusinessException(ErrorCode, String, Throwable) 로 넘어온 원인 예외다.
	 * 여기서 log.error 의 마지막 인자로 넘기지 않으면 그 cause 는 어디에도 출력되지 않는다.
	 *
	 * 로그에 errorCode.getMessage() 를 따로 찍지 않는 이유: 그 값은 code 로 이미 식별되는 고정 문구이고,
	 * BusinessException(ErrorCode) 1개 인자 생성자는 super(errorCode.getMessage())를 부르므로
	 * 그런 예외에서는 detail 과 글자 그대로 같은 문자열이 된다.
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();

		if (errorCode.getStatus().is5xxServerError()) {
			log.error("[BusinessException] code={}, detail={}", errorCode.name(), e.getMessage(), e);
		} else {
			log.warn("[BusinessException] code={}, detail={}", errorCode.name(), e.getMessage());
		}

		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}

}
