package org.example.be.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private int id;

    private String email;

    private String password;

    private String name;

    private String role;

    private Boolean policy;

    private Boolean subscribe;
}
