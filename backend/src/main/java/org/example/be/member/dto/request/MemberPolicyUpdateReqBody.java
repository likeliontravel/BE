package org.example.be.member.dto.request;

import jakarta.validation.constraints.NotNull;

public record MemberPolicyUpdateReqBody(
	@NotNull
	Boolean policyAgreed
) {
}
