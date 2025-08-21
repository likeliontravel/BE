package org.example.be.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.be.chat.entity.ChatMessage;

import java.time.LocalDateTime;

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
