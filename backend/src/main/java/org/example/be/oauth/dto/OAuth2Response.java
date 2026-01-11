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
    String getProviderId(); // 이건 social_user 테이블에서 식별하기 위한 것
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
     * OAuth2 제공자로부터 가져온 사용자 프로필 사진 반환.
     * @return 사용자 프로필 사진 미디어파일
     */
    String getProfileImage();
}