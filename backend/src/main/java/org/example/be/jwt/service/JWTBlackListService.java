package org.example.be.jwt.service;

import java.time.Duration;
import java.util.Map;

import org.example.be.jwt.util.JwtUt;
import org.example.be.member.service.AuthTokenService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JWTBlackListService {

	private final AuthTokenService authTokenService;
	private final StringRedisTemplate stringRedisTemplate;

	public void addBlackList(String accessToken) {
		if (accessToken == null || accessToken.isBlank()) {
			return;
		}

		Map<String, Object> claims = authTokenService.payload(accessToken);
		long exp = (long)claims.get("exp");
		long now = System.currentTimeMillis() / 1000;
		long ttl = exp - now;

		if (ttl > 0) {
			String accessTokenHash = JwtUt.sha256(accessToken);
			stringRedisTemplate.opsForValue().set(accessTokenHash, "logout", Duration.ofSeconds(ttl));
		}
	}

	public void isBlackList(String accessToken) {
		String accessTokenHash = JwtUt.sha256(accessToken);
		stringRedisTemplate.hasKey(accessTokenHash);
	}
}


