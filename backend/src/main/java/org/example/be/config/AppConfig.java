package org.example.be.config;

import org.example.be.jwt.util.JsonUt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Configuration
public class AppConfig {
	public static ObjectMapper objectMapper;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		AppConfig.objectMapper = objectMapper;
	}

	@PostConstruct // 빈(Bean)이 생성되고 의존성 주입이 끝난 직후에 딱 한 번 호출되는 초기화 훅(hook)
	public void postConstruct() {
		JsonUt.objectMapper = objectMapper;
	}
}