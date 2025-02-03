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
    private String password;     // 소셜은 빈문자열들어감
    private String role;
}
