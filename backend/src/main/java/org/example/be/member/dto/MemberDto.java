package org.example.be.member.dto;

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
}
