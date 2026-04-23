package org.example.be.domain.chat.dto;

import java.time.LocalDateTime;

import org.example.be.domain.chat.type.MessageType;

public record ChatRoomListWithLatestMessageResBody(
	String groupName,
	String latestMessage,
	LocalDateTime sendAt,
	MessageType type
) {
	public static ChatRoomListWithLatestMessageResBody from(String groupName, String latestMessage,
		LocalDateTime sendAt, MessageType type) {
		return new ChatRoomListWithLatestMessageResBody(groupName, latestMessage, sendAt, type);
	}
}
