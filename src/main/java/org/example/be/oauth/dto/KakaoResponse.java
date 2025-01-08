package org.example.be.oauth.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {
    private final Map<String, Object> attributes;

    public KakaoResponse(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        // "kakao_account"에서 "email" 키 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Object email = kakaoAccount.get("email");
            if (email != null) {
                return email.toString();
            }
        }
        return "Unknown"; // 기본값
    }

    @Override
    public String getName() {
        // 실명을 가져오기 위해 "kakao_account"에서 "name" 필드 사용
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Object name = kakaoAccount.get("name");
            if (name != null) {
                return name.toString();
            }
        }
        return "Unknown"; // 기본값
    }
}