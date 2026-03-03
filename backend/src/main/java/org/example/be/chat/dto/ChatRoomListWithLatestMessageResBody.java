package org.example.be.chat.dto;

import java.time.LocalDateTime;

import org.example.be.chat.type.MessageType;

import lombok.Builder;

@Builder
public record ChatRoomListWithLatestMessageResBody(
	String groupName,
	String latestMessage,
	LocalDateTime sendAt,
	MessageType type
) {
}
