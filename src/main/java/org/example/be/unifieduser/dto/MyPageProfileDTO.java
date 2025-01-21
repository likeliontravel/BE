package org.example.be.unifieduser.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageProfileDTO {
    private String email;             // 사용자 이메일
    private String name;              // 사용자 이름
    private String role;              // 사용자 권한 (ROLE_USER, ROLE_ADMIN 등)
    private Boolean policyAgreed;     // 이용 약관 동의 여부 (UnifiedUser에서 가져옴)
    private Boolean subscribed;       // 유료 구독 여부 (UnifiedUser에서 가져옴)
    private String provider;          // 소셜 로그인 제공자 정보 (소셜 사용자일 경우)
}
