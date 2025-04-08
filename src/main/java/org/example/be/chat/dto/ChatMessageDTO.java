package org.example.be.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long id;
    private String groupName;
    private String senderName;
    private String senderIdentifier;
    private String content;
    private String type;    // "TEXT" 또는 "IMAGE"
    private LocalDateTime sendAt;
}
