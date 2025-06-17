package org.example.be.group.invitation.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InvitationResponseDTO {
    private String invitationCode;  // 초대 코드
    private String invitationUrl;   // 초대 링크
    private LocalDateTime expiresAt;    // 만료시각
}
