package org.example.be.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberJoinReqBody(
	@NotBlank
	String name,

	@NotBlank
	@Email
	String email,

	@NotBlank
	@Size(min = 8, max = 16)
	String password
) {
}
