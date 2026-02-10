package org.example.be.member.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import org.example.be.jwt.util.JsonUt;
import org.example.be.jwt.util.JwtUt;
import org.example.be.member.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
	private final Clock clock = Clock.systemUTC();

	@Value("${spring.jwt.secret}")
	private String jwtSecretKey;
	@Value("${spring.accessToken.expire-seconds}")
	private int accessTokenExpireSeconds;
	@Value("${spring.refreshToken.expire-seconds}")
	private int refreshTokenExpireSeconds;

	/* =================== 액세스 토큰 (JWT) =================== */
	public String genAccessToken(Member member) {
		Map<String, Object> claims = Map.of(
			"id", member.getId(),
			"email", member.getEmail(),
			"name", member.getName(),
			"provider", member.getOauthProvider()
		);

		return JwtUt.toString(jwtSecretKey, accessTokenExpireSeconds, claims);
	}

	/** AT 파싱 */
	public Map<String, Object> payload(String accessToken) {
		return JwtUt.payload(jwtSecretKey, accessToken);
	}

	public String RefreshToken(Member member) {
		String jti = JwtUt.newOpaqueToken(64);
		Instant exp = Instant.now(clock).plusSeconds(refreshTokenExpireSeconds);
		String payloadJson = JsonUt.toString(Map.of(
			"userId", member.getId(),
			"exp", exp.getEpochSecond()
		));
		return jti + "." + payloadJson;
	}
}
