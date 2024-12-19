package org.example.be.oauth.service;


import org.example.be.oauth.dto.GoogleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service // 이 클래스는 Spring의 서비스 빈으로 등록되어 의존성 주입을 받는다.
public class GoogleOAuth2Service {

    // OAuth2 클라이언트의 설정 값을 외부 설정 파일(application.properties 또는 application.yml)에서 읽어온다.
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId; // 구글 클라이언트 ID

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret; // 구글 클라이언트 비밀 키

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri; // 구글 인증 후 리디렉션 URI

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String accessTokenUri; // 구글의 토큰 발급 URI

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri; // 구글 사용자 정보 조회 URI

    private final RestTemplate restTemplate; // HTTP 요청을 보내는 RestTemplate 객체

    // 생성자: RestTemplate을 의존성 주입 받아 초기화
    public GoogleOAuth2Service(RestTemplate restTemplate) {
        this.restTemplate = restTemplate; // RestTemplate을 초기화
    }

    /**
     * Google OAuth2 인증 후 받은 코드로 사용자 정보를 가져오는 메서드
     * @param code: 구글 OAuth2 인증에서 받은 인증 코드
     * @return GoogleResponse: 구글 사용자 정보를 담은 객체
     */
    public GoogleResponse getUserInfo(String code) {
        // 인증 코드를 사용하여 액세스 토큰을 받아옴
        String accessToken = getAccessToken(code);

        // 사용자 정보를 가져오기 위한 URL을 생성 (access_token을 쿼리 파라미터로 추가)
        String url = UriComponentsBuilder.fromHttpUrl(userInfoUri)
                .queryParam("access_token", accessToken) // 액세스 토큰을 쿼리 파라미터로 추가
                .toUriString();

        // GET 요청을 보내서 사용자 정보를 받아옴
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null) {
            throw new RuntimeException("Google API 응답이 비어있습니다.");
        }

        return new GoogleResponse(response);
    }

    /**
     * 인증 코드로 구글의 액세스 토큰을 얻는 메서드
     * @param code: 구글 OAuth2 인증에서 받은 인증 코드
     * @return 액세스 토큰
     */
    private String getAccessToken(String code) {
        // 액세스 토큰을 요청할 URL을 생성
        String url = UriComponentsBuilder.fromHttpUrl(accessTokenUri)
                .queryParam("grant_type", "authorization_code") // grant_type을 'authorization_code'로 설정
                .queryParam("client_id", clientId) // 클라이언트 ID
                .queryParam("client_secret", clientSecret) // 클라이언트 비밀 키
                .queryParam("redirect_uri", redirectUri) // 리디렉션 URI
                .queryParam("code", code) // 인증 코드
                .toUriString();

        // GET 요청을 보내서 토큰 정보를 받아옴
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        // 응답에서 access_token을 추출하여 반환
        return (String) response.get("access_token");
    }
}
