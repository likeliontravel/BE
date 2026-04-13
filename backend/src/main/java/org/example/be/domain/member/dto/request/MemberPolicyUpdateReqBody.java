package org.example.be.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;

public record MemberPolicyUpdateReqBody(
	@NotNull(message = "동의 여부를 입력해주세요.")
	Boolean policyAgreed
) {
}
