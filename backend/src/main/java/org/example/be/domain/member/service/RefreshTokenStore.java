package org.example.be.domain.member.service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.example.be.global.jwt.util.JsonUt;
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
			} catch (IllegalArgumentException e) {
				// JsonUt.parse()의 cause 체인 포함 로깅으로 3레이어 깊이의 원본 파싱, 받은 revokeRefresh에서도 체이닝 유지, 이걸 받을 필터(CustomAuthenticationFilter)에서 체인 전체 출력
				log.warn("[revokeRefresh] payload 파싱 실패 - user:sessions 정리 불가, jti={}", jti, e);
				return;
			} // 그 외 Exception 추가 필요 판단 시 사용할 때 자유롭게 추가해서 쓰셔요. (원래 Exception 크기로 잡던 걸 그대로 두면 NullPointerException까지 파싱 실패로 오인될 수 있어 명확히 앞에서 던질 수 있는 예외들로만 나눠뒀습니다.)

			if (payloadMap == null || !payloadMap.containsKey("userId") || payloadMap.get("userId") == null) {
				return;
			}
			long userId = ((Number)payloadMap.get("userId")).longValue();
			stringRedisTemplate.opsForSet().remove(KeyUser(userId), jti);
		}
	}

	public void revokeAllByUserId(long id) {
		String userKey = KeyUser(id);
		Set<String> jtis = stringRedisTemplate.opsForSet().members(userKey);

		if (jtis != null && !jtis.isEmpty()) {
			for (String jti : jtis) {
				stringRedisTemplate.delete(KeyRefresh(jti));
			}
		}

		stringRedisTemplate.delete(userKey);
	}
}
