package org.example.be.mail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MailVerifyReqBody(
	@NotBlank
	@Email
	String email,

	@NotBlank
	@Pattern(regexp = "\\d{6}")
	String code
) {
}
