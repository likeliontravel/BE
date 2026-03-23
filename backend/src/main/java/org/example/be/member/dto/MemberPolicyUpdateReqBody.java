package org.example.be.member.dto;

import jakarta.validation.constraints.NotNull;

public record MemberPolicyUpdateReqBody(
	@NotNull
	Boolean policyAgreed
) {
}
