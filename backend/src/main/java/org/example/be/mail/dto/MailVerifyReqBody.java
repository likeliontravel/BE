package org.example.be.mail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MailVerifyReqBody(
	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "유효한 이메일 주소가 아닙니다.")
	String email,

	@NotBlank(message = "인증코드를 입력해주세요.")
	@Pattern(regexp = "\\d{6}")
	String code
) {
}
