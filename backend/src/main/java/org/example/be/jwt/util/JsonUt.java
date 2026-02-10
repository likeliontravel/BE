package org.example.be.jwt.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUt {
	public static ObjectMapper objectMapper = new ObjectMapper();

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
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid JSON", e);
		}
	}
}
