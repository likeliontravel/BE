package org.example.be.member.dto;

import org.example.be.member.entity.Member;

public record MemberDto(
	Long id,
	String email,
	String name,
	String profileImageUrl,
	String role,
	boolean policyAgreed,
	boolean subscribed,
	String oauthProvider
) {
	public static MemberDto from(Member member) {
		return new MemberDto(
			member.getId(),
			member.getEmail(),
			member.getName(),
			member.getProfileImageUrl(),
			member.getRole().name(),
			Boolean.TRUE.equals(member.getPolicyAgreed()),
			Boolean.TRUE.equals(member.getSubscribed()),
			member.getOauthProvider() != null
				? member.getOauthProvider().name()
				: null
		);
	}
}

