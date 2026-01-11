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
        Object providerId = attributes.get("id");
        if (providerId == null) {
            throw new IllegalArgumentException("KakaoLogin - ProviderId is missing");
        }
        return providerId.toString();
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
        // email 못찾으면 예외 던지기
        throw new IllegalArgumentException("KakaoLogin - Email is missing");
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
        // name 못찾으면 예외 던지기
        throw new IllegalArgumentException("KakaoLogin - Name is missing");
    }

    @Override
    public String getProfileImage() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null && profile.get("profile_image_url") != null) {
                return profile.get("profile_image_url").toString();
            }
        }
        return null;
    }

}
