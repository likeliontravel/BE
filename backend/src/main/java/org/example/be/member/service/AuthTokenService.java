package org.example.be.member.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.example.be.jwt.util.JsonUt;
import org.example.be.jwt.util.JwtUt;
import org.example.be.member.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
	private final RefreshTokenStore refreshTokenStore;
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
		refreshTokenStore.saveRefresh(jti, member.getId(), Duration.ofSeconds(refreshTokenExpireSeconds), payloadJson);
		return jti;
	}

	public String rotateRefresh(String oldJti) {
		String payload = refreshTokenStore.findRefreshPayload(oldJti);
		if (payload == null)
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Token 입니다.");

		long userId = ((Number)JsonUt.parse(payload, Map.class).get("userId")).longValue();
		refreshTokenStore.deleteRefresh(oldJti, userId);

		String newJti = JwtUt.newOpaqueToken(64);
		Instant exp = Instant.now(clock).plusSeconds(refreshTokenExpireSeconds);
		String newPayloadJson = JsonUt.toString(Map.of(
			"userId", userId,
			"exp", exp.getEpochSecond()
		));
		refreshTokenStore.saveRefresh(newJti, userId, Duration.ofSeconds(refreshTokenExpireSeconds), newPayloadJson);
		return newJti;
	}

	public long findRefreshOwner(String jti) {
		String payload = refreshTokenStore.findRefreshPayload(jti);
		if (payload == null)
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Token 입니다.");

		return ((Number)JsonUt.parse(payload, Map.class).get("userId")).longValue();
	}
}
