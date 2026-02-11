package org.example.be.member.service;

import java.time.Duration;
import java.util.Map;

import javax.annotation.Nullable;

import org.example.be.jwt.util.JsonUt;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	public void revokeRefresh(String refreshPlain) {
		if (refreshPlain == null || refreshPlain.isBlank()) {
			return;
		}

		String jti = refreshPlain;
		String kRefresh = KeyRefresh(jti);

		String payloadJson = stringRedisTemplate.opsForValue().get(kRefresh);
		stringRedisTemplate.delete(kRefresh);

		if (payloadJson != null) {
			Map<String, Object> payloadMap = null;
			try {
				payloadMap = JsonUt.parse(payloadJson, Map.class);
			} catch (Exception e) {
				return;
			}

			if (payloadMap == null || !payloadMap.containsKey("userId") || payloadMap.get("userId") == null) {
				return;
			}
			long userId = ((Number)payloadMap.get("userId")).longValue();
			stringRedisTemplate.opsForSet().remove(KeyUser(userId), jti);
		}
	}
}
