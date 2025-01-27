package org.example.be.unifieduser.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnifiedUserProfileDTO {
    private String email;
    private String name;
    private Boolean subscribed;     // 구독 여부만 포함
    private String additionalInfo; // 소셜 유저인지 일반 유저인지 구분
    private String provider;       // 소셜 제공자 이름 (예: Google, Kakao 등)
    private String password;        // 비밀번호 추가

}
