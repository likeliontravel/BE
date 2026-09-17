package org.example.be.global.exception.handler;

import java.util.stream.Collectors;

import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
 * 이 순서 계약은 ExceptionHandlerOrderIT 가 고정한다. (Fallback 을 앞으로 옮기거나 부모-자식 타입 쌍을 다른 advice 로 가르면 그 테스트가 실패한다)
 *
 * === 413 퇴화 함정 ===
 * MaxUploadSizeExceededException 은 MultipartException 의 하위 타입이다.
 * 둘은 반드시 이 파일에 '함께' 둘 것. 다른 advice 로 갈라놓으면 @Order 선착순 매칭 때문에
 * 413(업로드 상한 초과)이 400(잘못된 multipart 요청)으로 퇴화한다.
 * 같은 advice 안에서는 스프링이 ExceptionDepthComparator 로 더 구체적인 쪽을 고르므로 안전하다.
 * 프론트가 413을 실제로 분기하고 있으므로 (채팅 이미지 업로드) 이 함정은 곧바로 사용자 영향으로 이어진다.
 *
 * === 가드레일 오탐 함정 ===
 * HandlerMethodValidationException 은 ResponseStatusException 의 하위 타입이다.
 * 이 둘도 반드시 이 파일에 '함께' 둘 것. 갈라놓으면 메서드 파라미터 검증 실패(400)가
 * ResponseStatusException 가드레일로 먼저 가서 '규칙 위반' 로그가 잘못 찍힌다.
 * 이 파일에는 부모-자식 타입이 함께 있는 쌍이 이렇게 2개다. 핸들러를 다른 advice 로 옮길 때는 상속 관계부터 확인할 것.
 *
 * 응답에는 ErrorCode 고정 문구와 파라미터, 헤더, 파트의 '이름'까지만 담는다.
 * 사용자가 보낸 값, 요청 경로, SQL, 스프링 예외 원문(Java 타입명이 들어 있다)은 로그에만 남긴다.
 * 여기 핸들러는 전부 사용자 요청 문제(4xx)라 WARN 한 줄로 남기고 스택은 남기지 않는다. (가드레일만 예외)
 *
 * 핸들러는 전부 3개 인자 CommonResponse.error(status, code, message) 를 쓴다. (이유: BusinessExceptionHandler 상단 주석)
 * 여기 등록되지 않은 프레임워크 예외는 FallbackExceptionHandler 에 잡혀 500이 된다.
 */
@Slf4j
@Order(20)
@RestControllerAdvice
@RequiredArgsConstructor
public class RequestExceptionHandler {

	// 업로드 크기 초과 안내 메시지에 실제 설정값을 담기 위해 주입한다.
	// ( spring.servlet.multipart.* 를 바인딩해둔 스프링 부트 자동설정 빈 )
	// 상한값을 하드코딩하지 않으므로 application.yml 을 바꾸면 안내 메시지도 함께 따라간다.
	private final MultipartProperties multipartProperties;

	// ===== 요청 본문 · 검증 =====

	/**
	 * 요청 본문 검증 실패 처리 - @Valid 위반을 400으로 응답한다.
	 * 래퍼의 @NotEmpty, 원소의 @NotBlank/@NotNull/@Min 등 cascade 위반도 모두 여기로 들어온다.
	 *
	 * 앱 전역 영향: 이 핸들러 추가 전에는 @Valid 실패가 catch-all(Exception)에 잡혀서
	 * 500 INTERNAL_SERVER_ERROR 로 나가고 있었다.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CommonResponse<Void>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {
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
		log.warn("[MethodArgumentNotValidException] {}", message);
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), message));
	}

	/**
	 * 메서드 파라미터 검증 실패 처리 - 400으로 응답한다.
	 * @RequestParam, @PathVariable 등에 @Min, @NotBlank 같은 제약을 직접 붙이면 (스프링 6.1+ 내장 메서드 검증)
	 * MethodArgumentNotValidException 이 아니라 이 예외가 던져진다.
	 * 현재 컨트롤러에는 파라미터 제약이 0곳이라 발동하지 않는다. 처음 제약을 붙이는 순간 500이 되지 않도록 미리 둔다.
	 *
	 * ResponseStatusException 의 하위 타입이다. 가드레일과 반드시 같은 advice 에 둘 것. (클래스 상단 가드레일 오탐 함정)
	 */
	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<CommonResponse<Void>> handleHandlerMethodValidationException(
		HandlerMethodValidationException e) {
		ErrorCode errorCode = ErrorCode.BAD_REQUEST;
		// 위반 메시지를 모아 하나의 문자열로 변환 (MethodArgumentNotValidException 핸들러와 같은 방식)
		String message = e.getAllErrors().stream()
			.map(MessageSourceResolvable::getDefaultMessage)
			.filter(defaultMessage -> defaultMessage != null)
			.collect(Collectors.joining(", "));
		if (message.isBlank()) {
			message = errorCode.getMessage();
		}
		log.warn("[HandlerMethodValidationException] {}", message);
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), message));
	}

	/**
	 * 요청 본문을 읽을 수 없음 - 400으로 응답한다.
	 * 깨진 JSON, 필드 타입 불일치 ("age": "abc"), 없는 enum 값, 필수 본문 (@RequestBody) 자체의 누락이 전부 여기로 온다.
	 *
	 * e.getMessage() 에는 Jackson 원문(Java 클래스명, 필드 경로) 이 들어 있으므로 로그에만 남긴다.
	 * BusinessException 을 cause 로 품은 경우는 BusinessExceptionHandler(@Order 10)가 먼저 가져간다. (그 파일 상단 cause 체인 주의)
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<CommonResponse<Void>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException e) {
		ErrorCode errorCode = ErrorCode.INVALID_REQUEST_BODY;
		log.warn("[HttpMessageNotReadableException] {}", e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}

	/**
	 * 지원하지 않는 Content-Type - 415 로 응답한다.
	 * @RequestBody 엔드포인트에 JSON 이 아닌 형식 (text/plain, form 등)으로 보내면 여기로 온다.
	 *
	 * 응답 헤더에 받을 수 있는 형식(Accept)을 싣는다. 그 값은 스프링이 이미 계산해서 e.getHeaders() 로 준다.
	 */
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<CommonResponse<Void>> handleHttpMediaTypeNotSupportedException(
		HttpMediaTypeNotSupportedException e) {
		ErrorCode errorCode = ErrorCode.UNSUPPORTED_MEDIA_TYPE;
		log.warn("[HttpMediaTypeNotSupportedException] contentType={}, supported={}", e.getContentType(),
			e.getSupportedMediaTypes());
		return ResponseEntity.status(errorCode.getStatus())
			.headers(e.getHeaders())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}

	// ===== 요청 파라미터 · 헤더 =====

	/**
	 * 필수 요청 파라미터 누락 처리 - 400으로 응답한다.
	 *
	 * 같은 advice 안에서는 스프링이 예외 계층 상 가장 구체적인 @ExceptionHandler 를 고르지만,
	 * advice 사이에서는 @Order 선착순이다. 이 핸들러가 없거나 이 advice 가 Fallback 보다 뒤에 오면
	 * catch-all(Exception)에 잡혀 500으로 나간다.
	 *
	 * 응답에는 파라미터 '이름'만 붙인다. 아래 예시처럼 스프링이 내놓는 e.getMessage() 원문에는 Java 타입명이 들어 있어 응답에 쓰지 않는다.
	 * (예: "Required request parameter 'groupName' for method parameter type String is not present")
	 * 파일 (MultipartFile) 타입 파라미터 누락은 이 예외가 아니라 MissingServletRequestPartException 으로 온다.
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<CommonResponse<Void>> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException e) {
		ErrorCode errorCode = ErrorCode.MISSING_REQUIRED_PARAMETER;
		String message = String.format("%s (%s)", errorCode.getMessage(), e.getParameterName());
		log.warn("[MissingServletRequestParameterException] {}", e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), message));
	}

	/**
	 * 필수 요청 헤더 누락 - 400으로 응답한다.
	 * @RequestHeader 의 required 가 true(기본값)인 헤더가 없으면 여기로 온다.
	 * 현재 @RequestHeader 는 Last-Event-ID(required = false) 1곳뿐이라 발동하지 않는다. 필수 헤더를 처음 받는 순간 500이 되지 않도록 미리 둔다.
	 */
	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<CommonResponse<Void>> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
		ErrorCode errorCode = ErrorCode.MISSING_REQUIRED_HEADER;
		String message = String.format("%s (%s)", errorCode.getMessage(), e.getHeaderName());
		log.warn("[MissingRequestHeaderException] {}", e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), message));
	}

	/**
	 * 파라미터 타입 변환 실패 - 400으로 응답한다.
	 * Long 자리에 문자가 오는 경우 등이다. (@PathVariable, @RequestParam 공통)
	 * 예: GET /board/list 라고 잘못 요청을 보냄 -> @GetMapping("/{id}") 에 매칭된 뒤 "list" 를 Long 으로 바꾸지 못해 여기로 온다.
	 *
	 * 응답에는 파라미터 이름만 붙이고, 사용자가 보낸 값은 응답에 되비추지 않는다.(값은 로그에서만)
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<CommonResponse<Void>> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException e) {
		ErrorCode errorCode = ErrorCode.INVALID_URI_VARIABLES;
		String message = String.format("%s (%s)", errorCode.getMessage(), e.getName());
		log.warn("[MethodArgumentTypeMismatchException] name={}, value={}, requiredType={}",
			e.getName(), e.getValue(), e.getRequiredType());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), message));
	}

	// ===== 라우팅 =====

	/**
	 * 존재하지 않는 경로 - 404로 응답한다.
	 * 어떤 컨트롤러에서도 매칭되지 않은 요청은 정적 리소스 핸들러로 넘어가고, 거기서도 없으면 이 예외가 던져진다.
	 * 인증이 필요한 경로는 시큐리티 필터가 먼저 401로 막으므로, 여기까지 오는 것은 공개 경로이거나 인증된 요청이다.
	 *
	 * 요청 경로는 사용자 입력이라 응답에 되비추지 않는다.
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<CommonResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
		ErrorCode errorCode = ErrorCode.ENDPOINT_NOT_FOUND;
		log.warn("[NoResourceFoundException] method={}, path={}", e.getHttpMethod(), e.getResourcePath());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}

	/**
	 * 지원하지 않는 HTTP 메서드 - 405로 응답한다.
	 * 경로는 맞는데 메서드가 틀린 경우이다. 예: POST 전용인 /mail/send 를 GET 으로 호출
	 *
	 * HTTP 표준(RFC 9110)은 405 응답에 허용 메서드 목록(Allow 헤더)을 반드시 싣도록 한다.
	 * 그 값은 스프링이 이미 계산해서 e.getHeaders() 로 주므로 그대로 쓴다.
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<CommonResponse<Void>> handleHttpRequestMethodNotSupportedException(
		HttpRequestMethodNotSupportedException e) {
		ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
		log.warn("[HttpRequestMethodNotSupportedException] method={}, supported={}", e.getMethod(),
			e.getSupportedHttpMethods());
		return ResponseEntity.status(errorCode.getStatus())
			.headers(e.getHeaders())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}

	// ===== 파일 업로드 (multipart) =====
	// 아래 세 핸들러를 갈라놓지 말 것 (클래스 상단 413 퇴화 함정)

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
	 * 필수 multipart 파트 누락 - 400으로 응답한다.
	 * @RequestParam 이 MultipartFile / List<MultipartFile> 타입이면 스프링은
	 * MissingServletRequestParameterException 이 아니라 이 예외를 던진다. (RequestParamMethodArgumentResolver.handleMissingValueInternal)
	 * 두 예외는 상속 관계가 아니라 형제라 서로를 커버하지 못한다.
	 * 예: POST /board/images 에 파일을 files 가 아닌 다른 이름으로 보냄
	 */
	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<CommonResponse<Void>> handleMissingServletRequestPartException(
		MissingServletRequestPartException e) {
		ErrorCode errorCode = ErrorCode.MISSING_REQUIRED_PART;
		String message = String.format("%s (%s)", errorCode.getMessage(), e.getRequestPartName());
		log.warn("[MissingServletRequestPartException] {}", e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), message));
	}

	/**
	 * multipart 요청 처리 실패 - 400으로 응답한다.
	 * 파일 파라미터를 받는 엔드포인트를 multipart 가 아닌 Content-Type 으로 호출하면 여기로 온다.
	 * 예: POST /board/images 를 application/json 으로 호출
	 *
	 * MaxUploadSizeExceededException(413)의 부모 타입이다. 위 413 핸들러와 반드시 같은 advice 에 둘 것.
	 */
	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<CommonResponse<Void>> handleMultipartException(MultipartException e) {
		ErrorCode errorCode = ErrorCode.INVALID_MULTIPART_REQUEST;
		log.warn("[MultipartException] {}", e.getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}

	// ===== 데이터 =====

	/**
	 * DB 제약 조건 위반 - 409로 응답한다.
	 * UNIQUE 중복, NOT NULL 위반, 컬럼 길이 초과 등을 스프링이 JPA/JDBC 예외에서 번역해 던진다.
	 * 앱 레벨 검증 (EMAIL_ALREADY_REGISTERED 등)을 통과한 뒤 DB 에서 걸린 것이므로, 동시 요청 경합이거나 검증 누락의 신호다.
	 *
	 * 모든 경우에 409가 정확하지는 않다. 예: 게시글 제목 51자는 컬럼 길이 초과라 여기로 오지만 본래 400이 맞다.
	 * 근본 해결은 요청 DTO 에 @Size 같은 제약을 붙여 DB 에 닿기 전에 걸러내는 것이다.
	 *
	 * 응답은 고정 문구만 쓴다. 예외 원문에는 실행된 SQL 과 중복된 값(이메일 등)이 들어 있다.
	 * 로그에는 가장 안쪽 원인 (DB 드라이버 메시지: 어느 컬럼, 어느 키)만 한 줄로 남긴다.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<CommonResponse<Void>> handleDataIntegrityViolationException(
		DataIntegrityViolationException e) {
		ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;
		log.warn("[DataIntegrityViolationException] cause={}", e.getMostSpecificCause().getMessage());
		return ResponseEntity.status(errorCode.getStatus())
			.body(CommonResponse.error(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage()));
	}

	// ===== 가드레일 =====

	/**
	 * 가드레일: ResponseStatusException 이 올라온 경우에 발동. 평소에는 발동하지 않는다.
	 * 우리 코드는 BusinessException 만 던진다는 규칙이 있다. 이 예외가 보인다면 규칙 위반이므로 로그로 드러낸다.
	 *
	 * 응답 규칙
	 * - 상태코드는 던진 쪽이 정한 값을 그대로 살린다. (401을 의도했는데 500이 나가면 기능이 조용히 깨진다)
	 * - code, message 는 ErrorCode 고정값을 쓴다. 5xx 면 INTERNAL_SERVER_ERROR, 그 외는 BAD_REQUEST.
	 *   code 값은 ErrorCode 상수 이름이라는 계약과, 예외 메시지를 응답에 담지 않는다는 원칙을 지키기 위해서다.
	 *   그래서 상태는 404인데 code는 BAD_REQUEST 일 수 있다. 규칙 위반을 고치면 사라지는 불일치다.
	 * - reason 은 로그에만 남긴다.
	 *
	 * 로그에 예외 클래스 이름을 찍는 이유: 스프링도 ResponseStatusException 의 하위 타입을 던진다. (HandlerMethodValidationException 등)
	 * 전용 핸들러가 없는 하위 타입이 여기로 오면, 우리 코드의 규칙 위반과 구분할 수 있어야 한다.
	 * 스택을 남기는 이유: 어디서 던졌는지 찾아 BusinessException 으로 바꿔야 하기 때문이다.
	 * 로그 레벨은 BusinessExceptionHandler 와 같은 기준이다. 5xx 는 ERROR, 그 외는 WARN.
	 */
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<CommonResponse<Void>> handleResponseStatusException(ResponseStatusException e) {
		HttpStatusCode statusCode = e.getStatusCode();
		ErrorCode errorCode = statusCode.is5xxServerError() ? ErrorCode.INTERNAL_SERVER_ERROR : ErrorCode.BAD_REQUEST;

		if (statusCode.is5xxServerError()) {
			log.error("[ResponseStatusException] 규칙 위반 - BusinessException 으로 교체할 것. type={}, status={}, reason={}",
				e.getClass().getName(), statusCode.value(), e.getReason(), e);
		} else {
			log.warn("[ResponseStatusException] 규칙 위반 - BusinessException 으로 교체할 것. type={}, status={}, reason={}",
				e.getClass().getName(), statusCode.value(), e.getReason(), e);
		}

		return ResponseEntity.status(statusCode)
			.body(CommonResponse.error(statusCode.value(), errorCode.name(), errorCode.getMessage()));
	}
}
