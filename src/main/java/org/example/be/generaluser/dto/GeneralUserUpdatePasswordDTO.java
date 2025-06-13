package org.example.be.generaluser.dto;

import lombok.Getter;

@Getter
public class GeneralUserUpdatePasswordDTO {
    private String email;
    private String password;
}
