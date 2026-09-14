package org.example.be.global.exception.handler;

import java.util.stream.Collectors;

import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.core.annotation.Order;
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
 * 스프링, 톰캣이 throw 한 프레임워크 예외 전담 advice.
 * 나누는 기준은 도메인이 아니라 "누가 throw 했는가"다. 우리가 던진 것은 BusinessExceptionHandler가 받는다.
 *
 * advice 조회 순서 (4개 파일 상단에 글자 그대로 동일하게 유지할 것)
 *
 *       @Order(0) SseExceptionHandler : SSE 클라이언트 이탈, 비동기 타임아웃
 *       @Order(10) BusinessExceptionHandler : 우리 코드가 throw 한 BusinessException
 *       @Order(20) RequestExceptionHandler : 스프링, 톰캣이 throw 한 프레임워크 예외
 *       @Order(LOWEST_PRECEDENCE) FallbackExceptionHandler : catch-all (Exception)
 *
 * advice 사이에서는 '가장 구체적인 핸들러'가 아니라 '@Order 선착순'으로 결정된다.
 * catch-all 을 가진 Fallback 이 앞에 오면 나머지 세 advice 가 통째로 무력화되어 모든 응답이 500이 된다.
 * 순서가 '필수'인 것은 Fallback 이 마지막이라는 점 하나뿐이다. (나머지 셋은 잡는 예외가 서로 달라 겹치지 않음)
 *
 * === 413 퇴화 함정 ===
 * MaxUploadSizeExceededException 은 MultipartException 의 하위 타입이다.
 * 둘은 반드시 이 파일에 '함께' 둘 것. 다른 advice 로 갈라놓으면 @Order 선착순 매칭 때문에
 * 413(업로드 상한 초과)이 400(잘못된 multipart 요청)으로 퇴화한다.
 * 같은 advice 안에서는 스프링이 ExceptionDepthComparator 로 더 구체적인 쪽을 고르므로 안전하다.
 * 프론트가 413을 실제로 분기하고 있으므로 (채팅 이미지 업로드) 이 함정은 곧바로 사용자 영향으로 이어진다.
 *
 * 핸들러는 전부 3개 인자 CommonResponse.error(status, code, message) 를 쓴다. (이유: BusinessExceptionHandler 상단 주석)
 */
// TODO: 등록되지 않은 프레임워크 예외 9종(잘못된 JSON, 타입 불일치, 없는 경로, 파일 파트 누락, multipart 아닌 요청 등)이
//       지금은 Fallback 에 잡혀 500 으로 나간다.
// - 왜 지금 안 하는가: 이 파일은 advice 분리(동작 무변경)만 담당하고, 상태코드가 바뀌는 변경은 따로 검증한다.
// - 올바른 해결: 예외별 전용 핸들러를 이 파일에 추가한다. MultipartException(400)은 반드시 이 파일에 둘 것 (위 413 함정).
// - 언제: Task 2-2(프레임워크 예외 9종 + ResponseStatusException 가드레일)
@Slf4j
@Order(20)
@RestControllerAdvice
@RequiredArgsConstructor
public class RequestExceptionHandler {

	// 업로드 크기 초과 안내 메시지에 실제 설정값을 담기 위해 주입한다.
	// ( spring.servlet.multipart.* 를 바인딩해둔 스프링 부트 자동설정 빈 )
	// 상한값을 하드코딩하지 않으므로 application.yml 을 바꾸면 안내 메시지도 함께 따라간다.
	private final MultipartProperties multipartProperties;

	/**
	 * 요청 본문 검증 실패 처리 - @Valid 위반을 400으로 응답한다.
	 * 래퍼의 @NotEmpty, 원소의 @NotBlank/@NotNull/@Min 등 cascade 위반도 모두 여기로 들어온다.
	 *
	 * 앱 전역 영향: 이 핸들러 추가 전에는 @Valid 실패가 catch-all(Exception)에 잡혀서
	 * 500 INTERNAL_SERVER_ERROR 로 나가고 있었다.
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
	 * MaxUploadSizeExceededException 은 max-file-size 초과와 max-request-size 초과 양쪽에서 던져진다.
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
	 * 같은 advice 안에서는 스프링이 예외 계층 상 가장 구체적인 @ExceptionHandler 를 고르지만,
	 * advice 사이에서는 @Order 선착순이다. 이 핸들러가 없거나 이 advice 가 Fallback 보다 뒤에 오면
	 * catch-all(Exception)에 잡혀 500으로 나간다.
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<CommonResponse<Void>> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException e) {
		ErrorCode errorCode = ErrorCode.BAD_REQUEST;
		log.warn("[MissingServletRequestParameterException] {}", e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}
}
