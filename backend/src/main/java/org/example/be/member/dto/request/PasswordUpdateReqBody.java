package org.example.be.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateReqBody(
	@Size(min = 8, max = 16)
	@NotBlank
	String oldPassword,

	@Size(min = 8, max = 16)
	@NotBlank
	String newPassword
) {
}
