package org.example.be.generaluser.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralUserDTO {

    private Long id;

    private String email;

    private String password;

    private String name;

    private String role;

    private String userIdentifier;
}
