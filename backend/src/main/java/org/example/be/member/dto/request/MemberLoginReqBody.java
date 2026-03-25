package org.example.be.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberLoginReqBody(
	@NotBlank(message = "이메일을 입력해주세요.")
	@Email
	String email,

	@NotBlank(message = "비밀번호를 입력해주세요.")
	@Size(min = 8)
	String password
) {
}
