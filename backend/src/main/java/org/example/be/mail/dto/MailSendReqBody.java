package org.example.be.mail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MailSendReqBody(
	@NotBlank
	@Email
	String email
) {
}

