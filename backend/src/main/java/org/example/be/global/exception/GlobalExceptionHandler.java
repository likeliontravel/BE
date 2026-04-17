package org.example.be.global.exception;

import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.springframework.http.ResponseEntity;
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
	 *  예상치 못한 예외 처리 - 500 응답
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
		log.error("[UnhandledException] message={}", e.getMessage(), e);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.getMessage()));
	}

	//
	// ## RuntimeException도 확인해보기

}
