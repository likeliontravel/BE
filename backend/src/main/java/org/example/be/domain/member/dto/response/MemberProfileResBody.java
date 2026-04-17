package org.example.be.domain.member.dto.response;

import org.example.be.domain.member.entity.Member;

public record MemberProfileResBody(
	String name,
	String profileImageUrl
) {
	public static MemberProfileResBody from(Member member) {
		return new MemberProfileResBody(member.getName(), member.getProfileImageUrl());
	}
}
