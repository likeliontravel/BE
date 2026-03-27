package org.example.be.member.dto.response;

import org.example.be.member.entity.Member;

public record MemberProfileResBody(
	String name,
	String profileImageUrl
) {
	public static MemberProfileResBody from(Member member) {
		return new MemberProfileResBody(member.getName(), member.getProfileImageUrl());
	}
}
