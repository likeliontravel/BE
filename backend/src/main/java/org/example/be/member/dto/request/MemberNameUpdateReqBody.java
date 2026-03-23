package org.example.be.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberNameUpdateReqBody(
	@NotBlank
	String name
) {
}