package org.example.be.global.exception;

import java.util.stream.Collectors;

import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 전역 예외처리 핸들러
// 메시지는 서비스레이어에서 던지는대로 응답까지 전달
// 예외처리 아키텍쳐 : 기본 RestControllerAdvice사용, 일반@Controller로 잡혀도 JSON응답하도록 메서드에 @ResponseBody 붙이기
//  @MessageMapping붙은 웹소켓 예외는 감지 안되니 추후 다시 생각할 예정
// TODO: 프레임워크 예외 (MethodArgumentNotValidException, MaxUploadSizeExceededException)을 별도 advice로 분리해야 할 것 같음.
//   - 왜 지금 안 하는가: 이 둘은 우리가 throw하는 BusinessException과 성격이 다르지만(스프링/톰캣이 던짐),
//     분리는 업로드 상한 정상화 작업 범위를 넘는 리팩터링이라 별도 작업으로 미룸
//   - 올바른 해결: RequestExceptionHandler(@RestControllerAdvice + @Order(Ordered.HIGHEST_PRECEDENCE)) 신설 후
//     위 두 핸들러를 이관. 이 클래스에는 BusinessException + catch-all(Exception)만 남긴다.
//     @Order가 필수인 이유: advice 사이에서는 최적 매칭이 아니라 선착순이므로,
//     catch-all을 가진 이 클래스가 먼저 조회되면 구체적 핸들러가 흡수되어 500으로 나간다.
//   - 언제: 예외 핸들러 정리 작업 시 (ErrorCode의 도메인별 인터페이스 분리와 함께 검토)
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	// 업로드 크기 초과 안내 메시지에 실제 설정값을 담기 위해 주입한다.
	// ( spring.servlet.multipart.* 를 바인딩해둔 스프링 부트 자동설정 빈 )
	// 상한값을 하드코딩하지 않으므로 application.yml을 바꾸면 안내 메시지도 함께 따라간다.
	private final MultipartProperties multipartProperties;

	/**
	 *  비즈니스 예외 처리 - ErrorCode에 정의된 모든 에러를 여기서 처리
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		log.warn("[BusinessException] code={}, message={}, detail={}", errorCode.name(), errorCode.getMessage(),
			e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.getMessage()));
	}

	/**
	 *  요청 본문 검증 실패 처리 - @Valid 위반을 400으로 응답
	 *  (래퍼의 @NotEmpty, 원소의 @NotBlank/@NotNull/@Min 등 cascade 위반도 모두 여기로 들어옴)
	 *  앱 전역 영향: 이 핸들러 추가 전에는 @Valid 실패가 catch-all(Exception)에 잡혀서 500 INTERNAL_SERVER_ERROR로 나가고 있었다.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CommonResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
		// 필드별 위반 메시지를 모아 하나의 문자열로 변환 (예: schedulePlaces[0].contentId: 장소 ID를 입력해주세요."
		String message = e.getBindingResult().getFieldErrors().stream()
			.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
			.collect(Collectors.joining(", "));
		if (message.isBlank()) {
			// 원소(필드)가 아닌 객체 레벨 위반(일정 블록 수정 리스트 래퍼객체 @NotEmpty 등 일부) fallback
			message = e.getBindingResult().getAllErrors().stream()
				.map(ObjectError::getDefaultMessage)
				.collect(Collectors.joining(", "));
		}
		ErrorCode errorCode = ErrorCode.BAD_REQUEST;
		log.warn("[ValidationException] {}", message);
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), message));
	}

	/**
	 * 업로드 파일 크기 초과 처리 - multipart 상한 (max-file-size) 초과를 413으로 응답
	 * MaxUploadSizeExceededException은 스프링/톰캣이 multipart 파싱 단계에서 던지는 런타임 예외이므로
	 * 우리 코드가 BusinessException으로 감싸 던질 지점이 없다. 따라서 여기서 ErrorCode로 매핑한다.
	 * 이 핸들러가 없으면 아래 catch-all에 잡혀 500으로 나가고 사용자는 용량 초과라는 사실 자체를 알 수 없다.
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<CommonResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
		ErrorCode errorCode = ErrorCode.FILE_SIZE_EXCEEDED;
		long maxFileSizeMb = multipartProperties.getMaxFileSize().toMegabytes();
		String message = errorCode.getMessage() + " (최대 " + maxFileSizeMb + "MB)";
		log.warn("[MaxUploadSizeExceededException] maxFileSize={}MB, detail={}", maxFileSizeMb, e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), message));
	}

	/**
	 * SSE 등 비동기 요청이 타임아웃된 경우 - 이미 스트림이 끊긴 상태라 응답 바디를 쓰지 않는다.
	 * 없으면 catch-all(Exception)이 잡아 매번 500 스택트레이스를 남긴다.
	 */
	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public void handleAsyncTimeout(AsyncRequestTimeoutException e) {
		log.debug("[AsyncRequestTimeoutException] {}", e.getMessage());
	}

	/**
	 * SSE 클라이언트가 브라우저 종료·네트워크 끊김 등으로 이탈한 경우 - 정상적인 시나리오라 응답 바디를 쓰지 않는다.
	 * ClientAbortException도 IOException 하위 타입이라 여기서 함께 처리된다.
	 */
	@ExceptionHandler(java.io.IOException.class)
	public void handleBrokenPipe(java.io.IOException e) {
		log.debug("[IOException] SSE 클라이언트 이탈 - {}", e.getMessage());
	}

	/**
	 *  예상치 못한 예외 처리 - 500 응답
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
		log.error("[UnhandledException] message={}", e.getMessage(), e);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.getMessage()));
	}

}
