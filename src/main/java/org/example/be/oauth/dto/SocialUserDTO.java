package org.example.be.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SocialUserDTO {
    private Long id;
    private String email;  // 이메일 필드 추가
    private String name;
    private String provider;
    private String role;
    private String userIdentifier;

//      5필드용 생성자 임시주석처리
//    public SocialUserDTO(String email, String name, String provider, String role, String userIdentifier) {
//        this.email=email;
//        this.name=name;
//        this.provider=provider;
//        this.role=role;
//        this.userIdentifier=userIdentifier;
//    }
}
