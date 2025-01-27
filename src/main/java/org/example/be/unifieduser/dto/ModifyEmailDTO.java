package org.example.be.unifieduser.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifyEmailDTO {
    private String userIdentifier;
    private String newEmail;
}