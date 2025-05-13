package org.example.be.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.chat.dto.ChatMessageDTO;
import org.example.be.chat.entity.ChatMessage;
import org.example.be.chat.service.ChatMessageService;
import org.example.be.resolver.DecodedPathVariable;
import org.example.be.security.util.SecurityUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatMessageSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     *  클라이언트가 "/pub/chat/{groupName}" 으로 메시지를 보내면 이 메서드가 실행됨
     *   1. WebSocket에서 전송된 메시지를 수신
     *   2. 메시지 DB에 저장
     *   3. "/sub/chat/{groupName}"으로 구독한 클라이언트들에게 브로드캐스트
     */
    @MessageMapping("/chat/{groupName}")
    public void handleMessage(
            @DestinationVariable String groupName,
            ChatMessageDTO incomingMessage,
            Principal principal
    ) {
        // URI 인코딩된 그룹 명 디코딩하기
        String decodedGroupName = UriUtils.decode(groupName, StandardCharsets.UTF_8);

        // 핸드셰이크 때 저장해둔 Principal에서 userIdentifier 추출
        String userIdentifier = principal.getName();

        if (userIdentifier == null) {
            throw new IllegalArgumentException("WebSocket 세션에 사용자 정보가 없습니다.");
        }

        // 메시지를 DB에 저장
        ChatMessage savedMessage = chatMessageService.saveMessage(
                decodedGroupName,
                userIdentifier,
                incomingMessage.getContent(),
                incomingMessage.getType()
        );

        // DTO 변환 후 해당 채팅방 구독자에게 브로드캐스팅
        ChatMessageDTO chatMessageDTO = chatMessageService.toDTO(savedMessage);
        messagingTemplate.convertAndSend("/sub/chat/" + groupName, chatMessageDTO);
    }
}