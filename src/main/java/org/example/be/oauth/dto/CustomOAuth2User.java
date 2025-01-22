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
        Map<String, Object> attributes = new HashMap<>();

        // 사용자 정보 추가
        attributes.put("username", socialUserDTO.getUsername());
        attributes.put("name", socialUserDTO.getName());
        attributes.put("email", socialUserDTO.getEmail()); // 이메일 추가

        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();

        // 권한 정보 추가
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return socialUserDTO.getRole();  // 권한 반환
            }
        });

        return collection;
    }

    @Override
    public String getName() {
        return socialUserDTO.getName(); // 여기서 name을 반환
    }

    public String getUsername() {

        return socialUserDTO.getUsername();
    }
}