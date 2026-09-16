package org.example.be.global.exception.handler;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.jwt.util.JwtUt;
import org.example.be.global.security.config.SecurityUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.Min;

// advice 조회 순서 계약 통합 테스트 - 예외 처리 리팩터링 전체의 안전망
//
// advice 사이에서는 '가장 구체적인 핸들러'가 아니라 '@Order 선착순'으로 핸들러가 정해진다. 이 규칙은 코드를 읽어서는 보이지 않는다.
// 이 테스트가 깨지면 둘 중 하나다.
// 1. catch-all(FallbackExceptionHandler) 이 앞으로 와서 구체 핸들러를 삼켰다 -> 전부 500
// 2. 부모-자식 타입 쌍이 다른 advice 로 갈라졌다 -> 413 이 400 으로 퇴화, 메서드 검증 400 이 가드레일로 오탐
//
// 실제 엔드포인트로 유발할 수 없는 케이스(메서드 파라미터 제약 0곳, ResponseStatusException 0곳)는
// 아래 테스트 전용 컨트롤러로 유발한다. 이 컨트롤러는 @Import 한 이 테스트의 컨텍스트에만 등록된다.
// (스프링 부트 테스트의 TestTypeExcludeFilter 가 테스트 클래스의 nested 클래스를 컴포넌트 스캔에서 제외한다)
//
// webEnvironment = RANDOM_PORT 인 이유: 업로드 상한(413)은 톰캣이 multipart 를 파싱할 때만 발생한다.
// MockMvc 의 multipart() 는 이미 파싱된 요청 객체를 만들어 톰캣 파싱을 건너뛰므로 max-file-size 가 적용되지 않는다.
// 그래서 413 케이스만 실제 톰캣(랜덤 포트)에 TestRestTemplate 으로 11MB 를 보낸다. 나머지는 MockMvc 로 충분하다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(ExceptionHandlerOrderIT.ExceptionTriggerController.class)
@Tag("integration")
@DisplayName("advice 조회 순서 계약 통합 테스트")
class ExceptionHandlerOrderIT {

	private static final long USER_ID = 10L;

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private TestRestTemplate restTemplate;
	@Autowired
	private ObjectMapper objectMapper;

	@Value("${spring.jwt.secret}")
	private String jwtSecret;

	// ===== BusinessExceptionHandler (@Order 10) =====

	@Test
	@DisplayName("로그인 실패(BusinessException)는 401 LOGIN_FAILED 이다")
	void businessException_loginFailure_returns401() throws Exception {
		String body = "{\"email\":\"order-it-unknown@example.com\",\"password\":\"Wrong-pass1!\"}";

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
	}

	// ===== RequestExceptionHandler (@Order 20) =====

	@Test
	@DisplayName("깨진 JSON 바디는 400 INVALID_REQUEST_BODY 이다")
	void requestException_brokenJson_returns400() throws Exception {
		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
	}

	// 아래 multipart 관련 3종(파라미터 누락, 파트 누락, multipart 아님)은 서로 상속 관계가 없는 형제라 서로를 커버하지 못한다.

	@Test
	@DisplayName("일반 @RequestParam 누락은 400 MISSING_REQUIRED_PARAMETER 이다")
	void requestException_missingParameter_returns400() throws Exception {
		mockMvc.perform(post("/group/addMember").with(authedUser()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("MISSING_REQUIRED_PARAMETER"))
			.andExpect(jsonPath("$.message").value(ErrorCode.MISSING_REQUIRED_PARAMETER.getMessage() + " (groupName)"));
	}

	@Test
	@DisplayName("파일 파트 이름이 틀리면 400 MISSING_REQUIRED_PART 이다")
	void requestException_missingPart_returns400() throws Exception {
		MockMultipartFile wrongNamePart = new MockMultipartFile("wrong", "a.png", MediaType.IMAGE_PNG_VALUE,
			new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/board/images").file(wrongNamePart).with(authedUser()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("MISSING_REQUIRED_PART"))
			.andExpect(jsonPath("$.message").value(ErrorCode.MISSING_REQUIRED_PART.getMessage() + " (files)"));
	}

	@Test
	@DisplayName("파일 업로드 엔드포인트를 JSON 으로 부르면 400 INVALID_MULTIPART_REQUEST 이다")
	void requestException_notMultipart_returns400() throws Exception {
		mockMvc.perform(post("/board/images")
				.with(authedUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_MULTIPART_REQUEST"));
	}

	@Test
	@DisplayName("★ 11MB 업로드는 413 FILE_SIZE_EXCEEDED 이며 400 으로 퇴화하지 않는다 (실제 톰캣 전송)")
	void requestException_uploadOverLimit_returns413NotDegradedTo400() throws Exception {
		// 가장 중요한 퇴행 방지 케이스다. 프론트가 채팅 이미지 업로드에서 413 을 실제로 분기한다.
		// MaxUploadSizeExceededException(413) 은 MultipartException(400) 의 하위 타입이라,
		// 둘이 다른 advice 로 갈라지면 선착순 매칭 때문에 400 으로 퇴화한다.
		MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("files", new ByteArrayResource(new byte[11 * 1024 * 1024]))
			.filename("large.png")
			.contentType(MediaType.IMAGE_PNG);
		MultiValueMap<String, HttpEntity<?>> multipartBody = multipartBodyBuilder.build();

		// 실제 HTTP 요청이라 MockMvc 의 authentication() 을 쓸 수 없다. 인증 필터는 토큰의 claims 만으로 인증하므로
		// (DB, Redis 조회 없음) 테스트에서 직접 서명한 토큰으로 충분하다.
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setBearerAuth(JwtUt.toString(jwtSecret, 60,
			Map.of("id", USER_ID, "email", "tester@example.com", "name", "tester", "role", "USER")));

		// multipart 파싱은 핸들러를 찾기 전에 일어나므로 이 요청은 컨트롤러, DB, GCS 에 닿지 않는다.
		ResponseEntity<String> response = restTemplate.exchange("/board/images", HttpMethod.POST,
			new HttpEntity<>(multipartBody, headers), String.class);

		assertThat(response.getStatusCode().value()).isEqualTo(413);
		JsonNode body = objectMapper.readTree(response.getBody());
		assertThat(body.get("code").asText()).isEqualTo("FILE_SIZE_EXCEEDED");
		assertThat(body.get("message").asText()).startsWith(ErrorCode.FILE_SIZE_EXCEEDED.getMessage());
	}

	@Test
	@DisplayName("메서드 파라미터 검증 실패는 400 이며 ResponseStatusException 가드레일로 새지 않는다")
	void requestException_methodValidation_notCaughtByGuardrail() throws Exception {
		// HandlerMethodValidationException 은 ResponseStatusException 의 하위 타입이다. (두 번째 부모-자식 쌍)
		// 가드레일로 샜다면 code 는 똑같이 BAD_REQUEST 지만 message 가 고정 문구("잘못된 요청입니다.")가 된다.
		// 그래서 message 가 위반 문구 그대로인지로 구분한다.
		mockMvc.perform(get("/test/exception-order/method-validation").param("page", "0").with(authedUser()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("BAD_REQUEST"))
			.andExpect(jsonPath("$.message").value(ExceptionTriggerController.PAGE_MIN_MESSAGE));
	}

	@Test
	@DisplayName("가드레일: ResponseStatusException(404) 은 상태 404 를 유지하고 code 는 BAD_REQUEST 로 고정한다")
	void requestException_responseStatusGuardrail_keepsStatus() throws Exception {
		// 테스트 로그에 '규칙 위반' WARN 과 스택이 찍히는 것이 정상이다.
		mockMvc.perform(get("/test/exception-order/response-status").with(authedUser()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.code").value("BAD_REQUEST"))
			.andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST.getMessage()));
	}

	// ===== FallbackExceptionHandler (@Order LOWEST_PRECEDENCE) =====

	@Test
	@DisplayName("어느 핸들러에도 해당하지 않는 예외는 Fallback 이 500 INTERNAL_SERVER_ERROR 로 받는다")
	void fallback_unclassifiedException_returns500() throws Exception {
		// 테스트 로그에 [UnhandledException] ERROR 와 스택이 찍히는 것이 정상이다.
		mockMvc.perform(get("/test/exception-order/unclassified").with(authedUser()))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
	}

	// 인증된 사용자(SecurityUser principal)를 SecurityContext 에 주입한다. (CSRF 는 disable 이라 불필요)
	private static RequestPostProcessor authedUser() {
		SecurityUser principal = new SecurityUser(USER_ID, "tester@example.com", "pw", "tester", List.of());
		return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
	}

	// 실제 엔드포인트로는 유발할 수 없는 예외를 던지는 테스트 전용 컨트롤러. 이 테스트 컨텍스트에만 등록된다.
	@RestController
	@RequestMapping("/test/exception-order")
	static class ExceptionTriggerController {

		static final String PAGE_MIN_MESSAGE = "page 는 1 이상이어야 합니다.";

		// 파라미터에 제약 어노테이션을 직접 붙이면 스프링 내장 메서드 검증이 HandlerMethodValidationException 을 던진다.
		// @RequestParam 이름을 명시한 이유: 생략하면 컴파일 옵션(-parameters)에 따라 파라미터 이름을 못 찾아 500 이 된다.
		@GetMapping("/method-validation")
		public String methodValidation(@RequestParam("page") @Min(value = 1, message = PAGE_MIN_MESSAGE) int page) {
			return "ok";
		}

		@GetMapping("/response-status")
		public String responseStatus() {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "테스트용 규칙 위반");
		}

		@GetMapping("/unclassified")
		public String unclassified() {
			throw new IllegalStateException("테스트용 미분류 예외");
		}
	}
}
