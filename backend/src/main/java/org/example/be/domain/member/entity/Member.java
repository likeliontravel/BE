package org.example.be.domain.member.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.example.be.global.entity.Base;
import org.example.be.domain.member.type.MemberRole;
import org.example.be.domain.member.type.OauthProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member")
public class Member extends Base {

	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = false)
	private String name;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private MemberRole role;

	@Column(nullable = false)
	private String password;

	@Column
	private LocalDateTime passwordChangedAt;  // 비밀번호 변경 시각

	@Column(nullable = false)
	private Boolean policyAgreed = false;   // 이용약관 동의여부 ; 기본값 false

	@Column(nullable = false)
	private Boolean subscribed = false;     // 유료구독 가입여부 ; 기본값 false

	@Enumerated(EnumType.STRING)
	private OauthProvider oauthProvider;

	public static Member createForJoin(String email, String name, String password) {
		return new Member(email, name, null, MemberRole.USER, password, LocalDateTime.now(), false, false,
			OauthProvider.General);
	}

	public static Member createForOAuth(String name, String email, String profileImgUrl, OauthProvider oauthProvider) {
		return new Member(email, name, profileImgUrl, MemberRole.USER, "", LocalDateTime.now(), false, false,
			oauthProvider);
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	public void changePassword(String newEncodedPassword) {
		this.password = newEncodedPassword;
		this.passwordChangedAt = LocalDateTime.now();
	}

	public void changeName(String name) {
		this.name = name;
	}

	public void updateProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public void updatePolicyAgreed(Boolean policyAgreed) {
		this.policyAgreed = policyAgreed;
	}

	public void updateSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}
}
