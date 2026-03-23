package org.example.be.member.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberNameUpdateReqBody(
	@NotBlank
	String name
) {
}