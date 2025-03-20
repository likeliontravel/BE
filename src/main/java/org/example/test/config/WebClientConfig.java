package org.example.test.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
public class WebClientConfig {

    @Value("${tourapi.base-url}")
    private String baseUrl;

    @Value("${tourapi.service-key}")
    private String serviceKey;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(baseUrl)
                .filter((request, next) -> {
                    // API 요청 시 URL에 `serviceKey` 자동 추가
                    return next.exchange(
                            ClientRequest.from(request)
                                    .url(addServiceKey(request.url()))
                                    .build()
                    );
                })
                .build();
    }

    private URI addServiceKey(URI uri) {
        return UriComponentsBuilder.fromUri(uri)
                .queryParam("serviceKey", serviceKey) // 중복 제거 후 WebClient에서 자동 추가
                .build(true)
                .toUri();
    }
}