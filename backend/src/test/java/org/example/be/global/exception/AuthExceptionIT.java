package org.example.be.global.exception;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.repository.MemberRepository;
import org.example.be.domain.member.type.OauthProvider;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.jwt.util.JwtUt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

// 인증 경로 예외 회귀 통합 테스트 (예외 처리 리팩터링 A1, A2)
// A1: 로그인 실패가 401 대신 500 으로 나가던 문제 (catch-all 이 ResponseStatusException 을 삼킴) - Task 1-1
// A2: 인증 필터가 모든 예외를 빈 바디 401 로 뭉개던 문제 - Task 1-2
//
// 서비스를 mock 으로 바꾸지 않고 실제 DB(로컬 MySQL)의 회원으로 로그인 경로를 끝까지 탄다.
// MockMvc 는 테스트와 같은 스레드에서 요청을 처리하므로 @Transactional 롤백이 적용되어 저장한 회원은 남지 않는다.
@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
@Transactional
@DisplayName("인증 경로 예외 응답 회귀 통합 테스트")
class AuthExceptionIT {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${spring.jwt.secret}")
	private String jwtSecret;

	@Test
	@DisplayName("틀린 비밀번호와 없는 이메일은 둘 다 401 LOGIN_FAILED 이고 응답 바디가 완전히 같다")
	void loginFailure_wrongPasswordAndUnknownEmail_returnIdenticalUnauthorized() throws Exception {
		String suffix = UUID.randomUUID().toString();
		String email = "auth-it-" + suffix + "@example.com";
		memberRepository.save(Member.createForJoin(email, "tester", passwordEncoder.encode("Correct-pass1!")));

		MvcResult wrongPassword = mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginBody(email, "Wrong-pass1!")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.status").value(401))
			.andExpect(jsonPath("$.code").value("LOGIN_FAILED"))
			.andExpect(jsonPath("$.message").value(ErrorCode.LOGIN_FAILED.getMessage()))
			.andReturn();

		MvcResult unknownEmail = mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginBody("auth-it-unknown-" + suffix + "@example.com", "Wrong-pass1!")))
			.andExpect(status().isUnauthorized())
			.andReturn();

		// 계정 열거 방지: 응답만 보고 "가입된 이메일인지" 알 수 없어야 한다. (두 경우의 구분은 서버 로그에만 남는다)
		assertThat(unknownEmail.getResponse().getContentAsString(StandardCharsets.UTF_8))
			.isEqualTo(wrongPassword.getResponse().getContentAsString(StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("소셜 가입 계정으로 이메일 로그인하면 500 이 아니라 400 SOCIAL_ACCOUNT_LOGIN_REQUIRED 이다")
	void loginFailure_socialAccount_returnsBadRequest() throws Exception {
		String email = "auth-it-social-" + UUID.randomUUID() + "@example.com";
		// 소셜 가입 계정은 password 가 빈 문자열이다. 그대로 인코더에 넘기면 IllegalArgumentException -> 500 이었다.
		memberRepository.save(Member.createForOAuth("social-tester", email, null, OauthProvider.KAKAO));

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginBody(email, "Any-password1!")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value("SOCIAL_ACCOUNT_LOGIN_REQUIRED"))
			.andExpect(jsonPath("$.message").value(ErrorCode.SOCIAL_ACCOUNT_LOGIN_REQUIRED.getMessage()));
	}

	@Test
	@DisplayName("만료된 액세스 토큰으로 보호 API 를 부르면 빈 바디가 아니라 401 CommonResponse JSON 이다")
	void expiredAccessToken_returnsUnauthorizedJson() throws Exception {
		Map<String, Object> claims = Map.of("id", 999_999_999L, "email", "expired@example.com", "name", "tester",
			"role", "USER");

		// 대조군: 같은 claims 의 '유효한' 토큰은 필터를 통과한다.
		// 이 단언이 있어야 아래 401 이 '토큰 헤더를 아예 안 읽어서'가 아니라 '만료를 판별해서' 난 것임이 보장된다.
		String validToken = JwtUt.toString(jwtSecret, 60, claims);
		mockMvc.perform(get("/schedule/nearest")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
			.andExpect(status().isOk());

		// expireSeconds 를 음수로 주면 발급 시점에 이미 만료된 토큰이 만들어진다.
		String expiredToken = JwtUt.toString(jwtSecret, -60, claims);
		mockMvc.perform(get("/schedule/nearest")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.status").value(401))
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	// MemberLoginReqBody 의 @Email, @Size(min = 8) 을 통과하는 값만 넘길 것. 걸리면 401/400 대신 @Valid 400 이 난다.
	private static String loginBody(String email, String password) {
		return String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
	}
}