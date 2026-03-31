package org.example.be.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateReqBody(
	@NotBlank(message = "현재 비밀번호를 입력해주세요.")
	@Size(min = 8, max = 16)
	String oldPassword,

	@NotBlank(message = "새 비밀번호를 입력해주세요.")
	@Size(min = 8, max = 16)
	String newPassword
) {
}
