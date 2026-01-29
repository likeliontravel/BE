package org.example.be.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberLoginReqBody(
	@NotBlank
	@Email
	String email,

	@NotBlank
	@Size(min = 8)
	String password
) {
}
