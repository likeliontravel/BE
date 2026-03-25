package org.example.be.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberNameUpdateReqBody(
	@NotBlank(message = "이름을 입력해주세요.")
	String name
) {
}