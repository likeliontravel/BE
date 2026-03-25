package org.example.be.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetSendReqBody(
	@NotBlank
	@Email
	String email
) {
}
