package org.example.be.chat.dto;

import java.time.LocalDateTime;

import org.example.be.chat.entity.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomListWithLatestMessageDTO {
	private String groupName;
	private String latestMessage;
	private LocalDateTime sendAt;
	private ChatMessage.MessageType type;
}
