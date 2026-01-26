package org.example.be.member.dto;

import org.example.be.member.entity.Member;
import org.example.be.member.type.MemberRole;

public record MemberDto(
	Long id,
	String email,
	String name,
	String profileImageUrl,
	MemberRole role,
	boolean policyAgreed,
	boolean subscribed,
	String oauthProvider
) {
	public static MemberDto from(Member member) {
		return new MemberDto(
			member.getId(),
			member.get(),
			member.getName(),
			member.getProfileImageUrl(),
			member.getRole(),
			member.isPolicyAgreed(),
			member.isSubscribed(),
			member.getProvider()
		);
	}
}

