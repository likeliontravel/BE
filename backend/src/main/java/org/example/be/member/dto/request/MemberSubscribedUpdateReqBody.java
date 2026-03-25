package org.example.be.member.dto.request;

import jakarta.validation.constraints.NotNull;

public record MemberSubscribedUpdateReqBody(
	@NotNull(message = "구독 여부를 입력해주세요.")
	Boolean subscribed
) {
}
