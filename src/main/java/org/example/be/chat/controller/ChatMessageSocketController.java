package org.example.be.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.chat.dto.ChatMessageDTO;
import org.example.be.chat.entity.ChatMessage;
import org.example.be.chat.service.ChatMessageService;
import org.example.be.security.util.SecurityUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     *  클라이언트가 "/pub/chat/{groupName}"으로 메시지를 보내면 실행됨.
     *  해당 메시지를 DB에 저장하고, "/sub/chat/{groupName}"을 구독중인 사람에게 메시지 전송
     */
    @MessageMapping("/chat/{groupName}")
    public void handleMessage(
            @DestinationVariable String groupName,
            ChatMessageDTO incomingMessage,
            Message<?> message
    ) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String senderIdentifier = (String) accessor.getSessionAttributes().get("userIdentifier");

        if (senderIdentifier == null) {
            throw new IllegalArgumentException("WebSocket 세션에 사용자 정보가 없습니다.");
        }

        // 메시지 저장
        ChatMessage savedMessage = chatMessageService.saveMessage(
                groupName,
                senderIdentifier,
                incomingMessage.getContent(),
                incomingMessage.getType()
        );

        // DTO로 변환
        ChatMessageDTO chatMessageDTO = chatMessageService.toDTO(savedMessage);

        System.out.println("보내기 직전 : " + chatMessageDTO);
        // 구독 중인 클라이언트에게 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chat/" + groupName, chatMessageDTO);
    }

}
