package org.example.be.domain.chat.dto;

import java.time.LocalDateTime;

import org.example.be.domain.chat.type.MessageType;

import lombok.Builder;

@Builder
public record ChatRoomListWithLatestMessageResBody(
	String groupName,
	String latestMessage,
	LocalDateTime sendAt,
	MessageType type
) {
}
