package org.example.be.oauth.dto;


import java.util.Map;

/**
 * Google OAuth2 인증을 통해 반환된 사용자 정보를 다루는 클래스.
 * - Google API의 JSON 응답을 파싱하여 필요한 데이터를 반환.
 * - OAuth2Response 인터페이스를 구현하여 다른 제공자와 동일한 방식으로 동작.
 */
public class GoogleResponse implements OAuth2Response {

    /**
     * Google API 응답에서 반환된 모든 속성을 저장.
     * - Map 형태로 제공되며, 필요한 정보는 key로 접근.
     */
    private final Map<String, Object> attribute;

    /**
     * 생성자: Google API 응답 데이터를 Map 형태로 초기화.
     * @param attribute Google API 응답 속성 (key-value 쌍)
     */
    public GoogleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    /**
     * Google 제공자 이름 반환.
     * - Google API를 사용 중이므로 "google"을 반환.
     * @return "google"
     */
    @Override
    public String getProvider() {
        return "google";
    }

    /**
     * Google에서 발급한 사용자 고유 ID 반환.
     * - Google API 응답의 "sub" 키 값을 반환.
     * @return 사용자 고유 ID (sub)
     */
    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    /**
     * Google에서 가져온 사용자 이메일 반환.
     * - Google API 응답의 "email" 키 값을 반환.
     * @return 사용자 이메일
     */
    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    /**
     * Google에서 가져온 사용자 이름 반환.
     * - Google API 응답의 "name" 키 값을 반환.
     * @return 사용자 이름
     */
    @Override
    public String getName() {
        return attribute.get("name").toString();
    }

    /**
     * Google API에서 반환된 전체 속성을 Map 형태로 반환.
     * - 추가적인 속성(예: 프로필 이미지, 로케일 등)에 접근 가능.
     * - 예: attribute.get("picture")로 프로필 이미지 URL을 가져올 수 있음.
     * @return Google API 응답 속성 (key-value 쌍)
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attribute;
    }
}