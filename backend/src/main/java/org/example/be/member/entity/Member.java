package org.example.be.member.entity;

import org.example.be.config.Base;
import org.example.be.member.type.MemberRole;
import org.example.be.member.type.OauthProvider;

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

	@Column(nullable = false)
	private Boolean policyAgreed = false;   // 이용약관 동의여부 ; 기본값 false

	@Column(nullable = false)
	private Boolean subscribed = false;     // 유료구독 가입여부 ; 기본값 false

	@Enumerated(EnumType.STRING)
	private OauthProvider oauthProvider;

	public static Member createForJoin(String email, String name, String password) {
		return new Member(email, name, null, MemberRole.USER, password, false, false, OauthProvider.General);
	}
}
