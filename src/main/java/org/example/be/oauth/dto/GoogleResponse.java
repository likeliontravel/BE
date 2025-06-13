package org.example.be.oauth.dto;

import java.util.Map;

/**
 * Google OAuth2 인증을 통해 반환된 사용자 정보를 다루는 클래스.
 * - Google API의 JSON 응답을 파싱하여 필요한 데이터를 반환.
 * - OAuth2Response 인터페이스를 구현하여 다른 제공자와 동일한 방식으로 동작.
 */
public class GoogleResponse implements OAuth2Response {

    private final Map<String, Object> attributes;

    public GoogleResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        Object providerId = attributes.get("sub");
        if (providerId == null) {
            throw new IllegalArgumentException("GoogleLogin - ProviderId is missing");
        }
        return providerId.toString();
    }

    @Override
    public String getEmail() {
        // Google 응답에서 "email" 필드를 사용하여 이메일 주소를 추출
        Object email = attributes.get("email");
        if (email == null) {
            throw new IllegalArgumentException("GoogleLogin - Email is missing");
        }
        return email.toString();
    }

    @Override
    public String getName() {
        // Google 응답에서 "name" 필드를 사용하여 사용자 이름을 추출
        Object name = attributes.get("name");
        if (name == null) {
            throw new IllegalArgumentException("GoogleLogin - Name is missing");
        }
        return name.toString();
    }

    @Override
    public String getProfileImage() {
        Object picture = attributes.get("picture");
        return picture != null ? picture.toString() : null;
    }

}