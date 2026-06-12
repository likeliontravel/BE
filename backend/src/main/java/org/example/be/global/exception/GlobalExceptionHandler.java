package org.example.be.global.exception;

import java.util.stream.Collectors;

import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

// 전역 예외처리 핸들러
// 메시지는 서비스레이어에서 던지는대로 응답까지 전달
// 예외처리 아키텍쳐 : 기본 RestControllerAdvice사용, 일반@Controller로 잡혀도 JSON응답하도록 메서드에 @ResponseBody 붙이기
//  @MessageMapping붙은 웹소켓 예외는 감지 안되니 추후 다시 생각할 예정
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
