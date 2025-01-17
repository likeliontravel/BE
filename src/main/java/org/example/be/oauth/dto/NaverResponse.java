package org.example.be.oauth.dto;

import java.util.Map;

public class NaverResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    // 네이버는 응답 JSON 형태에 사용자 데이터가 response라는 이름의 키 값으로 존재.
    public NaverResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        Object email = attribute.get("email");
        if (email == null) {
            throw new IllegalArgumentException("NaverLogin - email is missing");
        }
        return email.toString();
    }

    @Override
    public String getName() {
        Object name = attribute.get("name");
        if (name == null) {
            throw new IllegalArgumentException("NaverLogin - name is missing");
        }
        return name.toString();
}

}
