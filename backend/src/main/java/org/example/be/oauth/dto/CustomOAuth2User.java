package org.example.be.oauth.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.example.be.oauth.entity.SocialUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Getter;

@Getter
public class CustomOAuth2User implements OAuth2User {

	private final SocialUser socialUser;
	private final Map<String, Object> attributes;

	public CustomOAuth2User(SocialUser socialUser, Map<String, Object> attributes) {
		this.socialUser = socialUser;
		this.attributes = attributes;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(() -> socialUser.getRole());
	}

	@Override
	public String getName() {
		return socialUser.getName(); // 이름 반환
	}

	public String getEmail() {
		return socialUser.getEmail(); // 이메일 반환
	}

	public String getUserIdentifier() {
		return socialUser.getUserIdentifier();
	}
}