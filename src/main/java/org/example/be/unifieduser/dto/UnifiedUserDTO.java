package org.example.be.unifieduser.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnifiedUserDTO {
    private Long id;
    private String userIdentifier;
    private String email;
    private String name;
    private String role;
    private Boolean policyAgreed;   // 이용약관 동의 여부
    private Boolean subscribed; // 유료 구독 여부
}
