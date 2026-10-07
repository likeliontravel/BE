package org.example.be.global.jwt.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUt {
	public static ObjectMapper objectMapper;

	public static String toString(Object object) {
		return toString(object, null);
	}

	public static String toString(Object object, String defaultValue) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static <T> T parse(String json, Class<T> type) {
		try {
			return objectMapper.readValue(json, type);
		} catch (JsonProcessingException e) {
			// 변경: Exception -> JsonProcessingException 으로 구체화하여 변경하고 호출부로는 IllegalArgumentException으로 넘김
			// e.getMessage()가 아닌 e 전체를 cause 로 담아 예외 체이닝, 타입 정보와 함께 담아서 에러 원인 보존
			// 이 메서드를 쓰는 AuthTokenService.rotateRefresh()/findRefreshOwner()는 인증 필터에서만 호출되므로 예외가 advice(@RestControllerAdvice)에 도달하지 않는다.
			// 필터는 BusinessException 만 잡으므로, 이 IllegalArgumentException 은 컨테이너 /error 로 넘어가 500(스프링 기본 바디)이 된다.
			throw new IllegalArgumentException("Json 파싱 실패 - type=" + type.getSimpleName(), e);
		}
	}
}
