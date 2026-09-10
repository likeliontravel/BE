package org.example.be.global.exception;

import java.util.stream.Collectors;

import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리 advice. 컨트롤러, 서비스에서 올라온 예외를 CommonResponse 규격으로 변환한다.
 *
 * 응답에 나가는 문구와 로그에 남는 문구는 다르다
 * 		응답 <- ErrorCode.getMessage()	: 사용자에게 보여줄 고정 문구
 *  	로그 <- e.getMessage() 			: BusinessException 생성 시 넘긴 debugMessage
 *
 * 이 분리가 정보 노출을 막는다. 예를 들어 로그인 실패는 "없는 이메일"과 "비밀번호 불일치"가
 * 로그에서만 갈리고 응답은 완전히 같아서 (LOGIN_FAILED) 계정 열거 공격이 통하지 않는다.
 * 예외 메시지를 응답 바디에 그대로 담지 말 것 - SMTP 주소, Redis 연결 문자열 같은 서버 내부 정보가 그대로 샌다.
 * (실제로 MailController가 그렇게 짜여 있었다)
 *
 * 모든 핸들러는 3개의 인자 CommonResponse.error(status, code, message)를 쓴다.
 * code : 프론트가 분기에 쓰는 공개 계약이라, 일부 응답에만 실리면 프론트가 매번
 * "code가 없을 수도 있다"고 방어해야 해서 계약으로서 가치가 떨어진다.
 * 2개 인자 오버로드는 code를 모르는 호출부(필터 등)를 위해 남아 있는 것이지 advice 용이 아니다. **앞으로 CommonResponse의 error()는 꼭 3개 인자 사용할 것!**
 *
 * 이 advice의 사정거리 밖 - 여기서 잡히지 않는 예외
 * 		- 필터에서 난 예외			: DispatcherServlet 밖이라 컨테이너 error 디스패치로 간다 -> ErrorResponseWriter가 직접 규격 JSON을 쓰도록 했다.
 * 		- @MessageMapping(STOMP): @MessageExceptionHandler가 따로 있어야 한다 (현재 저장소에 0건)
 * 		- SSE 클라이언트 이탈		: SseExceptionHandler(@Order(0)으로 이 클래스보다 먼저 조회되도록 지정해둠)가 담당한다.
 */
// TODO: 프레임워크 예외 (MethodArgumentNotValidException, MaxUploadSizeExceededException)를 별도 advice로 분리한다.
// 	- 왜 지금 안 하는가: 이 둘은 스프링, 톰캣이 던지는 것이라 우리가 throw하는 BusinessException과
// 	성격이 다르지만, 분리는 advice 배치 전체를 건드리는 작업이라 별도 Task에서 진행한다.
// 	- 올바른 해결: RequestExceptionHandler(@RestControllerAdvice + @Order(Ordered.HIGHEST_PRECEDENCE)) 를
// 	신설해 이관하고, 이 클래스에는 BusinessException + catch-all(Exception)만 남긴다.
// 	@Order가 필수인 이유 : advice 사이에서는 최적 매칭이 아니라 선착순 조회라,
// 	catch-all을 가진 이 클래스가 먼저 걸리면 구체적 핸들러가 흡수되어 500으로 나가버린다.
// 	- 분리 시 함정: MaxUploadSizeExceededException은 MultipartException의 하위 타입이다.
// 	둘을 다른 advice로 갈라놓으면 선착순 매칭 때문에 413이 400으로 퇴화한다.
// 	프론트가 413을 실제로 분기하고 있으므로 (채팅 이미지 업로드) 반드시 같은 advice에 둘 것.
// 	- 언제: Task 2-1(advice 분리), Task 2-2(프레임워크 예외 9종 추가)
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	// 업로드 크기 초과 안내 메시지에 실제 설정값을 담기 위해 주입한다.
	// ( spring.servlet.multipart.* 를 바인딩해둔 스프링 부트 자동설정 빈 )
	// 상한값을 하드코딩하지 않으므로 application.yml을 바꾸면 안내 메시지도 함께 따라간다.
	private final MultipartProperties multipartProperties;

	/**
	 *  비즈니스 예외 처리 - ErrorCode에 정의된 모든 에러를 여기서 처리한다.
	 *
	 *  로그 레벨을 상태코드로 가른다.
	 *
	 *  	5xx : 우리 잘못이다. ERROR + stack trace로 남겨야 원인(타임아웃, 인증 실패 등)을 추적할 수 있다.
	 *  	4xx : 사용자 입력 문제라 예상된 흐름이다. 스택을 남기면 진짜 장애가 로그에 묻힌다.
	 *
	 *  cause는 BusinessException(ErrorCode, String, Throwable)로 넘어온 원인 예외다.
	 *  여기서 log.error의 마지막 인자로 넘기지 않으면 그 cause는 어디에도 출력되지 않는다.
	 *
	 *  로그에 errorCode.getMessage() 를 따로 찍지 않는 이유: 그 값은 code로
	 *  이미 식별되는 고정 문구이고, BusinessException(ErrorCode) 1개 인자 생성자는
	 *  super(errorCode.getMessage()) 를 부르므로 그런 예외에서는 detail과 글자 그대로 같은 문자열이 된다.
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

	/**
	 *  요청 본문 검증 실패 처리 - @Valid 위반을 400으로 응답한다.
	 *  래퍼의 @NotEmpty, 원소의 @NotBlank/@NotNull/@Min 등 cascade 위반도 모두 여기로 들어온다.
	 *
	 *  앱 전역 영향: 이 핸들러 추가 전에는 @Valid 실패가 catch-all(Exception)에 잡혀서
	 *  500 INTERNAL_SERVER_ERROR로 나가고 있었다.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CommonResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
		// 필드별 위반 메시지를 모아 하나의 문자열로 변환
		// (예: "schedulePlaces[0].contentId: 장소 ID를 입력해주세요.")
		String message = e.getBindingResult().getFieldErrors().stream()
			.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
			.collect(Collectors.joining(", "));
		if (message.isBlank()) {
			// 원소(필드)가 아닌 객체 레벨 위반 시 fallback
			// (일정 블록 수정 리스트 래퍼 객체의 @NotEmpty 등은 fieldErrors에 잡히지 않는다)
			message = e.getBindingResult().getAllErrors().stream()
				.map(ObjectError::getDefaultMessage)
				.collect(Collectors.joining(", "));
		}
		ErrorCode errorCode = ErrorCode.BAD_REQUEST;
		log.warn("[ValidationException] {}", message);
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), message));
	}

	/**
	 * 업로드 파일 크기 초과 처리 - multipart 상한 초과를 413으로 응답한다.
	 *
	 * MaxUploadSizeExceededException은 max-file-size 초과와 max-request-size 초과 양쪽에서 던져진다.
	 * 예외 객체만으로는 둘을 구분할 수 없으므로(구분하려면 톰캣 내부 예외 타입에 의존해야 함)
	 * 두 상한을 함께 안내해 사용자가 어느 쪽을 어겼는지 스스로 판단할 수 있게 한다.
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<CommonResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
		ErrorCode errorCode = ErrorCode.FILE_SIZE_EXCEEDED;
		long maxFileSizeMb = multipartProperties.getMaxFileSize().toMegabytes();
		long maxRequestSizeMb = multipartProperties.getMaxRequestSize().toMegabytes();
		String message = String.format("%s (파일 1개당 최대 %dMB, 요청 전체 최대 %dMB)",
			errorCode.getMessage(), maxFileSizeMb, maxRequestSizeMb);
		log.warn("[MaxUploadSizeExceededException] maxFileSize={}MB, maxRequestSize={}MB, detail={}",
			maxFileSizeMb, maxRequestSizeMb, e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), message));
	}

	/**
	 * 필수 요청 파라미터 누락 처리 - 400으로 응답한다.
	 *
	 * 같은 클래스 안에서는 Spring이 예외 계층상 가장 구체적인 @ExceptionHandler를 고르므로,
	 * 이 핸들러가 없으면 아래 catch-all(Exception)에 잡혀 500으로 나간다.
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<CommonResponse<Void>> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException e) {
		ErrorCode errorCode = ErrorCode.BAD_REQUEST;
		log.warn("[MissingServletRequestParameterException] {}", e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}

	// SSE 클라이언트 이탈 (IOException)과 비동기 타임아웃은 SseExceptionHandler가 담당한다.
	// 전역 핸들러로 되돌리지 말 것. - IOException은 상위 타입이라, 여기에 두면
	// SSE가 아닌 모든 컨트롤러의 I/O 실패까지 로그 없이 200 빈 바디로 끝난다.

	/**
	 *  예상치 못한 예외 처리 - 500 응답
	 *
	 *  여기 잡힌 것은 우리가 분류하지 못한 예외다. 최종 Fallback 역할. code(INTERNAL_SERVER_ERROR) 는
	 *  "서버에서 알 수 없는 오류가 났다"는 뜻이지 특정 원인을 가리키지 않는다.
	 *  같은 code가 로그에 반복해서 보인다면, 그 원인에 전용 핸들러를 붙이라는 신호로 읽을 것.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
		log.error("[UnhandledException] message={}", e.getMessage(), e);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}

}
