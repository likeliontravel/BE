package org.example.be.group.invitation.dto;

import java.time.LocalDateTime;

// 초대 링크 응답용 ResponseBody
public record InvitationResBody(
	String invitationCode,
	String invitationUrl,
	LocalDateTime expiresAt
) {
}
