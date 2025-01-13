package org.example.be.oauth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialUserDTO {

    private String role;
    private String name;
    private String username;
    private String userKey;
}
