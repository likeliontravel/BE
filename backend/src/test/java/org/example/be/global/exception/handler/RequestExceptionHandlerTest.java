package org.example.be.global.exception.handler;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

// RequestExceptionHandler 단위 테스트.
// 스프링 컨텍스트 없이 핸들러 14개를 직접 호출해 '예외 -> 상태코드, code, message, 로그' 변환만 검증한다.
// 어느 핸들러가 선택되는가(부모-자식 타입 쌍, advice @Order)는 여기서 검증할 수 없다. -> ExceptionHandlerOrderIT
//
// 공통으로 고정하는 규칙 (RequestExceptionHandler 클래스 상단 주석)
// 1. 응답에는 ErrorCode 고정 문구와 파라미터, 헤더, 파트의 '이름'까지만 담는다. 사용자 입력값, 경로, SQL, 스프링 원문은 담지 않는다.
// 2. 로그 접두어는 '[예외 단순명] ' 이다. 운영 로그 검색 키라 글자 그대로 고정한다.
// 3. 사용자 요청 문제(4xx)라 WARN 한 줄이고 스택은 남기지 않는다. (가드레일만 예외)
@DisplayName("RequestExceptionHandler 프레임워크 예외 변환 단위 테스트")
class RequestExceptionHandlerTest {

	private RequestExceptionHandler requestExceptionHandler;

	private final ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
	private final Logger handlerLogger = (Logger)LoggerFactory.getLogger(RequestExceptionHandler.class);

	@BeforeEach
	void setUp() {
		// application.yml 의 spring.servlet.multipart 설정과 같은 값
		MultipartProperties multipartProperties = new MultipartProperties();
		multipartProperties.setMaxFileSize(DataSize.ofMegabytes(10));
		multipartProperties.setMaxRequestSize(DataSize.ofMegabytes(55));
		requestExceptionHandler = new RequestExceptionHandler(multipartProperties);

		logAppender.start();
		handlerLogger.addAppender(logAppender);
	}

	@AfterEach
	void detachLogAppender() {
		handlerLogger.detachAppender(logAppender);
		logAppender.stop();
	}

	// ===== 요청 본문 · 검증 =====

	@Test
	@DisplayName("@Valid 필드 위반은 400 BAD_REQUEST 이고 message 에 '필드: 위반 문구'를 모아 담는다")
	void methodArgumentNotValid_fieldErrors_returns400WithFieldMessages() throws Exception {
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "memberJoinReqBody");
		bindingResult.addError(new FieldError("memberJoinReqBody", "email", "이메일을 입력해주세요."));
		bindingResult.addError(new FieldError("memberJoinReqBody", "password", "비밀번호는 16자 이하여야 합니다."));
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(dummyParameter(),
			bindingResult);

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleMethodArgumentNotValidException(exception);

		assertErrorResponse(response, 400, "BAD_REQUEST",
			"email: 이메일을 입력해주세요., password: 비밀번호는 16자 이하여야 합니다.");
		assertSingleWarnLogWithoutStack("[MethodArgumentNotValidException] ");
	}

	@Test
	@DisplayName("@Valid 객체 레벨 위반(필드 에러 없음)은 객체 위반 문구로 대체한다")
	void methodArgumentNotValid_objectErrorOnly_fallsBackToObjectMessage() throws Exception {
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(),
			"schedulePlaceListReqBody");
		bindingResult.addError(new ObjectError("schedulePlaceListReqBody", "일정 장소 목록이 비어 있습니다."));
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(dummyParameter(),
			bindingResult);

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleMethodArgumentNotValidException(exception);

		assertErrorResponse(response, 400, "BAD_REQUEST", "일정 장소 목록이 비어 있습니다.");
	}

	@Test
	@DisplayName("메서드 파라미터 검증 실패는 400 BAD_REQUEST 이고 위반 문구를 담는다 (가드레일 로그가 아니다)")
	void handlerMethodValidation_returns400WithViolationMessage() throws Exception {
		// mock 이 아니라 실제 예외 인스턴스를 만든다. @RequestParam @Min(1) page 에 0 이 들어온 상황
		// 뒤의 null 3개(container, index, key)는 List 원소 같은 컨테이너 검증일 때만 쓰인다.
		// 마지막 인자(sourceLookup)는 원본 위반 객체를 꺼낼 때만 쓰여 이 테스트와 무관하다.
		// (인자가 더 짧은 생성자 2개는 스프링 6.2 에서 제거 예정(deprecated for removal)이라 쓰지 않는다)
		ParameterValidationResult parameterResult = new ParameterValidationResult(dummyParameter(), 0,
			List.of(new DefaultMessageSourceResolvable(null, null, "1 이상이어야 합니다")),
			null, null, null, (error, sourceType) -> null);
		MethodValidationResult validationResult =
			MethodValidationResult.create(this, dummyMethod(), List.of(parameterResult));
		HandlerMethodValidationException exception = new HandlerMethodValidationException(validationResult);

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleHandlerMethodValidationException(exception);

		assertErrorResponse(response, 400, "BAD_REQUEST", "1 이상이어야 합니다");
		assertSingleWarnLogWithoutStack("[HandlerMethodValidationException] ");
	}

	@Test
	@DisplayName("읽을 수 없는 요청 본문은 400 INVALID_REQUEST_BODY 이고 Jackson 원문을 응답에 담지 않는다")
	void httpMessageNotReadable_returns400WithoutJacksonDetail() {
		String jacksonDetail = "JSON parse error: Unexpected end-of-input: expected close marker for Object"
			+ " (org.example.be.domain.member.dto.request.MemberLoginReqBody)";
		HttpMessageNotReadableException exception =
			new HttpMessageNotReadableException(jacksonDetail, new MockHttpInputMessage(new byte[0]));

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleHttpMessageNotReadableException(exception);

		assertErrorResponse(response, 400, "INVALID_REQUEST_BODY", ErrorCode.INVALID_REQUEST_BODY.getMessage());
		ILoggingEvent event = assertSingleWarnLogWithoutStack("[HttpMessageNotReadableException] ");
		// 원문은 로그에서만 볼 수 있어야 한다.
		assertThat(event.getFormattedMessage()).contains("MemberLoginReqBody");
	}

	@Test
	@DisplayName("지원하지 않는 Content-Type 은 415 UNSUPPORTED_MEDIA_TYPE 이고 Accept 헤더를 싣는다")
	void httpMediaTypeNotSupported_returns415WithAcceptHeader() {
		HttpMediaTypeNotSupportedException exception = new HttpMediaTypeNotSupportedException(
			MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON), HttpMethod.POST);

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleHttpMediaTypeNotSupportedException(exception);

		assertErrorResponse(response, 415, "UNSUPPORTED_MEDIA_TYPE", ErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage());
		assertThat(response.getHeaders().getFirst(HttpHeaders.ACCEPT)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
		assertSingleWarnLogWithoutStack("[HttpMediaTypeNotSupportedException] ");
	}

	// ===== 요청 파라미터 · 헤더 =====

	@Test
	@DisplayName("필수 파라미터 누락은 400 MISSING_REQUIRED_PARAMETER 이고 파라미터 '이름'만 붙인다")
	void missingServletRequestParameter_returns400WithParameterNameOnly() {
		MissingServletRequestParameterException exception =
			new MissingServletRequestParameterException("groupName", "String");

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleMissingServletRequestParameterException(exception);

		assertErrorResponse(response, 400, "MISSING_REQUIRED_PARAMETER",
			ErrorCode.MISSING_REQUIRED_PARAMETER.getMessage() + " (groupName)");
		// 스프링 원문 "Required request parameter 'groupName' for method parameter type String is not present" 가 새면 안 된다.
		assertThat(response.getBody().getMessage()).doesNotContain("Required").doesNotContain("String");
		assertSingleWarnLogWithoutStack("[MissingServletRequestParameterException] ");
	}

	@Test
	@DisplayName("필수 헤더 누락은 400 MISSING_REQUIRED_HEADER 이고 헤더 '이름'만 붙인다")
	void missingRequestHeader_returns400WithHeaderNameOnly() throws Exception {
		MissingRequestHeaderException exception = new MissingRequestHeaderException("X-Group-Id", dummyParameter());

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleMissingRequestHeaderException(exception);

		assertErrorResponse(response, 400, "MISSING_REQUIRED_HEADER",
			ErrorCode.MISSING_REQUIRED_HEADER.getMessage() + " (X-Group-Id)");
		assertSingleWarnLogWithoutStack("[MissingRequestHeaderException] ");
	}

	@Test
	@DisplayName("파라미터 타입 변환 실패는 400 INVALID_URI_VARIABLES 이고 사용자가 보낸 값은 응답에 되비추지 않는다")
	void methodArgumentTypeMismatch_returns400WithoutEchoingValue() throws Exception {
		MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
			"<script>", Long.class, "id", dummyParameter(),
			new NumberFormatException("For input string: \"<script>\""));

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleMethodArgumentTypeMismatchException(exception);

		assertErrorResponse(response, 400, "INVALID_URI_VARIABLES",
			ErrorCode.INVALID_URI_VARIABLES.getMessage() + " (id)");
		assertThat(response.getBody().getMessage()).doesNotContain("<script>");
		ILoggingEvent event = assertSingleWarnLogWithoutStack("[MethodArgumentTypeMismatchException] ");
		// 입력값은 로그에서만 볼 수 있어야 한다.
		assertThat(event.getFormattedMessage()).contains("value=<script>");
	}

	// ===== 라우팅 =====

	@Test
	@DisplayName("없는 경로는 404 ENDPOINT_NOT_FOUND 이고 요청 경로를 응답에 되비추지 않는다")
	void noResourceFound_returns404WithoutEchoingPath() {
		NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "mail/nope");

		ResponseEntity<CommonResponse<Void>> response = requestExceptionHandler.handleNoResourceFoundException(
			exception);

		assertErrorResponse(response, 404, "ENDPOINT_NOT_FOUND", ErrorCode.ENDPOINT_NOT_FOUND.getMessage());
		assertThat(response.getBody().getMessage()).doesNotContain("mail/nope");
		ILoggingEvent event = assertSingleWarnLogWithoutStack("[NoResourceFoundException] ");
		assertThat(event.getFormattedMessage()).contains("path=mail/nope");
	}

	@Test
	@DisplayName("지원하지 않는 HTTP 메서드는 405 METHOD_NOT_ALLOWED 이고 Allow 헤더를 싣는다")
	void httpRequestMethodNotSupported_returns405WithAllowHeader() {
		HttpRequestMethodNotSupportedException exception =
			new HttpRequestMethodNotSupportedException("GET", List.of("POST"));

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleHttpRequestMethodNotSupportedException(exception);

		assertErrorResponse(response, 405, "METHOD_NOT_ALLOWED", ErrorCode.METHOD_NOT_ALLOWED.getMessage());
		// RFC 9110 은 405 응답에 Allow 헤더를 요구한다.
		assertThat(response.getHeaders().getFirst(HttpHeaders.ALLOW)).isEqualTo("POST");
		assertSingleWarnLogWithoutStack("[HttpRequestMethodNotSupportedException] ");
	}

	// ===== 파일 업로드 (multipart) =====

	@Test
	@DisplayName("업로드 상한 초과는 413 FILE_SIZE_EXCEEDED 이고 설정된 두 상한을 함께 안내한다")
	void maxUploadSizeExceeded_returns413WithConfiguredLimits() {
		MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(
			DataSize.ofMegabytes(10).toBytes());

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleMaxUploadSizeExceededException(exception);

		assertErrorResponse(response, 413, "FILE_SIZE_EXCEEDED",
			ErrorCode.FILE_SIZE_EXCEEDED.getMessage() + " (파일 1개당 최대 10MB, 요청 전체 최대 55MB)");
		assertSingleWarnLogWithoutStack("[MaxUploadSizeExceededException] ");
	}

	@Test
	@DisplayName("필수 multipart 파트 누락은 400 MISSING_REQUIRED_PART 이고 파트 '이름'만 붙인다")
	void missingServletRequestPart_returns400WithPartNameOnly() {
		MissingServletRequestPartException exception = new MissingServletRequestPartException("files");

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleMissingServletRequestPartException(exception);

		assertErrorResponse(response, 400, "MISSING_REQUIRED_PART",
			ErrorCode.MISSING_REQUIRED_PART.getMessage() + " (files)");
		assertSingleWarnLogWithoutStack("[MissingServletRequestPartException] ");
	}

	@Test
	@DisplayName("multipart 가 아닌 요청은 400 INVALID_MULTIPART_REQUEST 이다")
	void multipart_returns400() {
		MultipartException exception = new MultipartException("Current request is not a multipart request");

		ResponseEntity<CommonResponse<Void>> response = requestExceptionHandler.handleMultipartException(exception);

		assertErrorResponse(response, 400, "INVALID_MULTIPART_REQUEST",
			ErrorCode.INVALID_MULTIPART_REQUEST.getMessage());
		assertSingleWarnLogWithoutStack("[MultipartException] ");
	}

	// ===== 데이터 =====

	@Test
	@DisplayName("DB 제약 위반은 409 DATA_INTEGRITY_VIOLATION 이고 SQL 을 응답에 담지 않으며 로그엔 가장 안쪽 원인만 남긴다")
	void dataIntegrityViolation_returns409WithoutSql() {
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
			"could not execute statement [update member set name=? where id=?]",
			new SQLException("Data too long for column 'name' at row 1"));

		ResponseEntity<CommonResponse<Void>> response =
			requestExceptionHandler.handleDataIntegrityViolationException(exception);

		assertErrorResponse(response, 409, "DATA_INTEGRITY_VIOLATION",
			ErrorCode.DATA_INTEGRITY_VIOLATION.getMessage());
		assertThat(response.getBody().getMessage()).doesNotContain("update member");
		ILoggingEvent event = assertSingleWarnLogWithoutStack("[DataIntegrityViolationException] ");
		assertThat(event.getFormattedMessage())
			.isEqualTo("[DataIntegrityViolationException] cause=Data too long for column 'name' at row 1");
	}

	// ===== 가드레일 =====

	@Test
	@DisplayName("가드레일(4xx): 상태코드는 던진 값 그대로, code·message 는 BAD_REQUEST 고정, reason 은 로그에만 + WARN 스택")
	void responseStatus_clientError_keepsStatusAndFixesCode() {
		ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글 없음 - boardId: 7");

		ResponseEntity<CommonResponse<Void>> response = requestExceptionHandler.handleResponseStatusException(
			exception);

		// 헤더 404 와 바디 status 404 가 일치해야 한다. (2-2 반영 중 바디 status 가 400 으로 어긋났던 결함)
		assertErrorResponse(response, 404, "BAD_REQUEST", ErrorCode.BAD_REQUEST.getMessage());
		assertThat(response.getBody().getMessage()).doesNotContain("boardId");

		assertThat(logAppender.list).hasSize(1);
		ILoggingEvent event = logAppender.list.get(0);
		assertThat(event.getLevel()).isEqualTo(Level.WARN);
		assertThat(event.getFormattedMessage()).startsWith("[ResponseStatusException] 규칙 위반")
			.contains("status=404")
			.contains("reason=게시글 없음 - boardId: 7");
		// 가드레일만 4xx 에서도 스택을 남긴다. 어디서 던졌는지 찾아 BusinessException 으로 바꿔야 하기 때문이다.
		assertThat(event.getThrowableProxy()).isNotNull();
	}

	@Test
	@DisplayName("가드레일(5xx): 상태코드는 던진 값 그대로, code·message 는 INTERNAL_SERVER_ERROR 고정 + ERROR 스택")
	void responseStatus_serverError_keepsStatusAndLogsError() {
		ResponseStatusException exception =
			new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "외부 API 점검 중");

		ResponseEntity<CommonResponse<Void>> response = requestExceptionHandler.handleResponseStatusException(
			exception);

		assertErrorResponse(response, 503, "INTERNAL_SERVER_ERROR", ErrorCode.INTERNAL_SERVER_ERROR.getMessage());

		assertThat(logAppender.list).hasSize(1);
		ILoggingEvent event = logAppender.list.get(0);
		assertThat(event.getLevel()).isEqualTo(Level.ERROR);
		assertThat(event.getFormattedMessage()).startsWith("[ResponseStatusException] 규칙 위반");
		assertThat(event.getThrowableProxy()).isNotNull();
	}

	// ===== 헬퍼 =====

	// 응답 헤더 상태코드와 바디 4필드를 한 번에 확인한다.
	private static void assertErrorResponse(ResponseEntity<CommonResponse<Void>> response,
		int expectedStatus, String expectedCode, String expectedMessage) {
		assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus);
		CommonResponse<Void> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.isSuccess()).isFalse();
		assertThat(body.getStatus()).isEqualTo(expectedStatus);
		assertThat(body.getCode()).isEqualTo(expectedCode);
		assertThat(body.getMessage()).isEqualTo(expectedMessage);
	}

	// 로그가 정확히 1건이고, WARN 이고, 접두어가 맞고, 스택이 없는지 확인한다. (4xx 핸들러 공통 규칙)
	private ILoggingEvent assertSingleWarnLogWithoutStack(String expectedPrefix) {
		assertThat(logAppender.list).hasSize(1);
		ILoggingEvent event = logAppender.list.get(0);
		assertThat(event.getLevel()).isEqualTo(Level.WARN);
		assertThat(event.getFormattedMessage()).startsWith(expectedPrefix);
		assertThat(event.getThrowableProxy()).isNull();
		return event;
	}

	// 일부 스프링 예외는 생성자에 MethodParameter 를 요구한다. 내용은 검증과 무관하므로 아래 더미 메서드의 첫 파라미터를 쓴다.
	private static MethodParameter dummyParameter() throws NoSuchMethodException {
		return new MethodParameter(dummyMethod(), 0);
	}

	private static Method dummyMethod() throws NoSuchMethodException {
		return RequestExceptionHandlerTest.class.getDeclaredMethod("dummyEndpoint", String.class);
	}

	@SuppressWarnings("unused")
	private void dummyEndpoint(String value) {
	}
}