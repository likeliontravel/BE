package org.example.be.member.service;

import java.time.Clock;
import java.util.Map;

import org.example.be.jwt.util.JwtUt;
import org.example.be.member.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
	private final MemberService memberService; // 권한 변경 시 authVersion 올릴 때 사용
	private final Clock clock = Clock.systemUTC();

	@Value("${spring.jwt.secret}")
	private String jwtSecretKey;
	@Value("${spring.accessToken.expire-seconds}")
	private int accessTokenExpireSeconds;

	/* =================== 액세스 토큰 (JWT) =================== */
	public String genAccessToken(Member member) {
		long id = member.getId();
		Map<String, Object> claims = Map.of(
			"id", member.getId(),
			"email", member.getEmail(),
			"name", member.getName(),
			"provider", member.getOauthProvider().toString(),
			"role", member.getRole()
		);

		return JwtUt.toString(jwtSecretKey, accessTokenExpireSeconds, claims);
	}

	/** AT 파싱 */
	public Map<String, Object> payload(String accessToken) {
		return JwtUt.payload(jwtSecretKey, accessToken);
	}

}
