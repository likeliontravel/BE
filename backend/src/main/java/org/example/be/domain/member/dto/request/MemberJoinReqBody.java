package org.example.be.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberJoinReqBody(
	@NotBlank(message = "이름을 입력해주세요.")
	String name,

	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "유효한 이메일 주소가 아닙니다.")
	String email,

	@NotBlank(message = "비밀번호를 입력해주세요.")
	@Size(min = 8, max = 16)
	String password
) {
}
