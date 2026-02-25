package org.example.be.chat.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
	private Long id;
	private String groupName;
	private Long senderId;
	private String content;
	private String type;    // "TEXT" 또는 "IMAGE"
	private LocalDateTime sendAt;
}
