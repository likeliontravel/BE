package org.example.be.member.service;

import java.time.Duration;

import javax.annotation.Nullable;

import org.example.be.jwt.util.JwtUt;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {
	private final StringRedisTemplate stringRedisTemplate;

	private String KeyRefresh(String jti) {
		return "refresh:" + jti;
	}

	private String KeyUser(Long id) {
		return "user:sessions:" + id;
	}

	public void saveRefresh(String jti, Long id, Duration ttl, String payloadJson) {
		stringRedisTemplate.opsForValue().set(KeyRefresh(jti), payloadJson, ttl);
		stringRedisTemplate.opsForSet().add(KeyUser(id), jti);
	}

	public @Nullable String findRefreshPayload(String oldJti) {
		return stringRedisTemplate.opsForValue().get(KeyRefresh(oldJti));
	}

	public void deleteRefresh(String oldJti, long userId) {
		stringRedisTemplate.delete(KeyRefresh(oldJti));
		stringRedisTemplate.opsForSet().remove(KeyUser(userId), oldJti);
	}

	public void revokeRefresh(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			return;
		}

		String jti = JwtUt.sha256(refreshToken);
		String kRefresh = KeyRefresh(jti);

		String userIdStr = stringRedisTemplate.opsForValue().get(kRefresh);
		stringRedisTemplate.delete(kRefresh);

		if (userIdStr != null) {
			Long userId = Long.parseLong(userIdStr);
			stringRedisTemplate.opsForSet().remove(KeyUser(userId), jti);
		}
	}
}
