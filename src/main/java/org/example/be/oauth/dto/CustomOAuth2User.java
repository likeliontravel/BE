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
        // 추가: SocialUserDTO 정보를 Map 형태로 반환
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", socialUserDTO.getUsername());
        attributes.put("name", socialUserDTO.getName());
        attributes.put("role", socialUserDTO.getRole());
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(() -> socialUserDTO.getRole());
        return collection;
    }

    @Override
    public String getName() {
        return socialUserDTO.getName();
    }

    public String getUsername() {
        return socialUserDTO.getUsername();
    }
}