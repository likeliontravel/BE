package org.example.be.oauth.dto;

/**
 * OAuth2 제공자(Google, Naver 등)의 사용자 정보를 표준화하여 다루기 위한 인터페이스.
 */
public interface OAuth2Response {

    /**
     * OAuth2 제공자 이름 반환 (예: google, naver).
     * @return 제공자 이름
     */
    String getProvider();

    /**
     * OAuth2 제공자가 발급한 고유 사용자 ID 반환.
     * @return 제공자에서 발급한 사용자 ID
     */
    String getProviderId();

    /**
     * OAuth2 제공자로부터 가져온 사용자 이메일 반환.
     * @return 사용자 이메일
     */
    String getEmail();

    /**
     * OAuth2 제공자로부터 가져온 사용자 실명 반환.
     * @return 사용자 이름
     */
    String getName();

    /**
     * OAuth2 제공자가 반환한 모든 속성을 Map 형태로 반환.
     * - 제공자마다 추가 정보가 다르기 때문에 확장성을 위해 사용.
     * - 예: Google의 경우 프로필 이미지, 언어 설정 등도 포함 가능.
     * @return 사용자 속성의 Key-Value 쌍을 포함한 Map
     */
//    Map<String, Object> getAttributes();
}