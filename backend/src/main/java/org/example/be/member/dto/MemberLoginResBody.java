package org.example.be.member.dto;

public record MemberLoginResBody(
	MemberDto member,
	String accessToken
) {
}
