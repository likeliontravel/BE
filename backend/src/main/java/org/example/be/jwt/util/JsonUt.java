package org.example.be.jwt.util;

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
			// 변경: Exception -> JsonProcessingException으로 구체화하여 변경하고 호출부로는 IllegalArgumentException으로 넘김
			// e.getMessage()가 아닌 e 전체를 cause로 담아 예외 체이닝, 타입 정보와 함께 담아서 에러 원인 보존
			// AuthTokenService의 메서드에서는 예외가 발생해도 GlobalExceptionHandler에 도달하지 않는다.(그 이전에 필터가 처리하고 따로 response를 만들지 않음)
			// RefreshTokenStore.revokeRefresh()는 아직 사용하지 않는 메서드로 보여서 기존대로 그냥 return;하도록 하되, 에러 로그만 추가
			throw new IllegalArgumentException("Json 파싱 실패 - type=" + type.getSimpleName(), e);
		}
	}
}
