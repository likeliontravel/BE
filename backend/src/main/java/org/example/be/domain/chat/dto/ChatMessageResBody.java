package org.example.be.domain.chat.dto;

import java.time.LocalDateTime;

import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.chat.type.MessageType;

import lombok.Builder;

@Builder
public record ChatMessageResBody(
	Long id,
	String groupName,
	Long senderId,
	String content,
	MessageType type,    // Enum 타입으로 변경
	LocalDateTime sendAt
) {
	public static ChatMessageResBody from(ChatMessage chatMessage) {
		return new ChatMessageResBody(
			chatMessage.getId(),
			chatMessage.getGroup().getGroupName(),
			chatMessage.getSender().getId(),
			chatMessage.getContent(),
			chatMessage.getType(),
			chatMessage.getCreatedTime()
		);
	}
}
