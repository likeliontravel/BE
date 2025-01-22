package org.example.be.oauth.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final SocialUserDTO socialUserDTO;

    public CustomOAuth2User(SocialUserDTO socialUserDTO) {
        this.socialUserDTO = socialUserDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return socialUserDTO.getRole();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return socialUserDTO.getName(); // 이름 반환
    }

    public String getUsername() {
        return socialUserDTO.getUsername(); // 고유 식별자로 username 반환
    }

    public String getEmail() {
        return socialUserDTO.getEmail(); // 이메일 반환
    }
}