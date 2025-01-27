package org.example.be.oauth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialUserDTO {
    private Long id;
    private String email;  // 이메일 필드 추가
    private String name;
    private String provider;
    private String role;
    private String userIdentifier;
}
