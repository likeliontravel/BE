package org.example.be.member.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordUpdateReqBody(
	@NotBlank
	String email,
	@NotBlank
	String password
) {
}
