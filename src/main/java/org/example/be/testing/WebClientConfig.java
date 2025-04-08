package org.example.be.testing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    // 빈 이름 충돌로 이름 직접 지정
    /* 사용처에서는 아래와 같은 형식으로 사용
        @Autowired
        @Qualifier("tourApiWebClient")
        private WebClient tourApiWebClient;
     */
    @Bean(name = "tourApiWebClient")
    public WebClient tourApiWebClient(WebClient.Builder builder) {
        return builder.baseUrl("http://apis.data.go.kr/B551011/KorService1")  // 정확한 기본 URL 설정
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
