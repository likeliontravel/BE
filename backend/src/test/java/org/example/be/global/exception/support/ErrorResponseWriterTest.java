package org.example.be.global.exception.support;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.example.be.global.exception.code.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// ErrorResponseWriter 단위 테스트.
// 필터, 시큐리티 엔트리포인트처럼 advice 에 도달하지 못하는 지점이 쓰는 유일한 에러 응답 통로다.
// 여기서 형식이 어긋나면 그 경로의 401/403 응답이 전부 CommonResponse 규격을 벗어난다.
// 운영에서는 RedisConfig 의 ObjectMapper 빈이 주입되지만, CommonResponse 에는 날짜 필드가 없어 기본 ObjectMapper 로 충분하다.
@DisplayName("ErrorResponseWriter 에러 응답 쓰기 단위 테스트")
class ErrorResponseWriterTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private ErrorResponseWriter errorResponseWriter;

	@BeforeEach
	void setUp() {
		errorResponseWriter = new ErrorResponseWriter(objectMapper);
	}

	@Test
	@DisplayName("ErrorCode 의 상태코드와 CommonResponse 규격 JSON 을 쓴다")
	void write_setsStatusAndCommonResponseJson() throws IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();

		errorResponseWriter.write(response, ErrorCode.UNAUTHORIZED);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);

		JsonNode body = objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));
		assertThat(body.get("success").asBoolean()).isFalse();
		assertThat(body.get("status").asInt()).isEqualTo(401);
		assertThat(body.get("code").asText()).isEqualTo("UNAUTHORIZED");
		assertThat(body.get("message").asText()).isEqualTo(ErrorCode.UNAUTHORIZED.getMessage());
		// 에러 응답에는 data 가 없다. (@JsonInclude(NON_NULL) 이라 키 자체가 빠진다)
		assertThat(body.has("data")).isFalse();
	}

	@Test
	@DisplayName("Content-Type 에 charset=UTF-8 을 명시하고 한글을 UTF-8 바이트로 쓴다")
	void write_declaresUtf8CharsetAndEncodesKorean() throws IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();

		errorResponseWriter.write(response, ErrorCode.UNAUTHORIZED);

		// 이 테스트의 핵심 단언은 'Content-Type 에 charset=UTF-8 이 붙어 있다'이다.
		// MockHttpServletResponse 는 Content-Type 이 JSON 이면 charset 을 지정하지 않아도 UTF-8 로 쓴다. (2026-09-16 실측)
		// 그래서 ErrorResponseWriter 의 setCharacterEncoding() 을 지워도 아래 한글 바이트 단언은 그대로 통과한다.
		// 지웠을 때 달라지는 것은 헤더뿐이다: "application/json;charset=UTF-8" -> "application/json"
		assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");

		String bodyAsUtf8 = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
		assertThat(bodyAsUtf8).contains(ErrorCode.UNAUTHORIZED.getMessage());
	}

	@Test
	@DisplayName("message 를 직접 넘겨도 code 와 상태코드는 ErrorCode 를 따른다")
	void write_withCustomMessage_keepsCodeAndStatusFromErrorCode() throws IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		String customMessage = "로그인이 만료되었습니다. 다시 로그인해주세요.";

		errorResponseWriter.write(response, ErrorCode.INVALID_TOKEN, customMessage);

		assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_TOKEN.getStatus().value());
		JsonNode body = objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));
		assertThat(body.get("code").asText()).isEqualTo("INVALID_TOKEN");
		assertThat(body.get("message").asText()).isEqualTo(customMessage);
	}

	@Test
	@DisplayName("이미 커밋된 응답에는 아무것도 쓰지 않는다")
	void write_committedResponse_writesNothing() throws IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		// SSE 스트림처럼 헤더와 본문 일부가 이미 클라이언트로 나간 상태를 흉내낸다.
		response.setCommitted(true);

		errorResponseWriter.write(response, ErrorCode.UNAUTHORIZED);

		// 이 테스트의 핵심 단언은 '본문이 비어 있다'이다.
		// MockHttpServletResponse.setStatus() 는 커밋된 응답이면 값을 무시하므로,
		// ErrorResponseWriter 의 isCommitted() 가드를 지워도 status 는 200 으로 남는다. (status 단언만으로는 가드 삭제를 못 잡는다)
		assertThat(response.getContentAsByteArray()).isEmpty();
		assertThat(response.getStatus()).isEqualTo(200);
	}
}