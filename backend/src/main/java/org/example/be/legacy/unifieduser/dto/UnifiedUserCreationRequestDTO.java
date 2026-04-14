package org.example.be.unifieduser.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UnifiedUserCreationRequestDTO {
    private String provider;
    private String email;
    private String name;
    private String role;
}
