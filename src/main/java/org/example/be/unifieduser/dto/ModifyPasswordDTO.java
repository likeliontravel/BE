package org.example.be.unifieduser.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifyPasswordDTO {
    private String userIdentifier;
    private String newPassword;
}