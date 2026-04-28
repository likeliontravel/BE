package org.example.be.domain.chat.controller;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

import org.example.be.domain.chat.dto.ChatMessageResBody;
import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.chat.service.ChatMessageService;
import org.example.be.global.security.config.SecurityUser;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.UriUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
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
		@Payload ChatMessageResBody incomingMessage,
		Principal principal
	) {

		// principal 자체를 먼저 체크 (user가 아닌 principal이 null 가능성 있음)
		if (principal == null) {
			log.warn("[WebSocket 인증 실패] principal이 null입니다. groupName: {}", groupName);
			return;  // 예외를 던지는 대신 early return
		}

		SecurityUser user = (SecurityUser)((UsernamePasswordAuthenticationToken)principal).getPrincipal();
		// URI 인코딩된 그룹 명 디코딩하기
		String decodedGroupName = UriUtils.decode(groupName, StandardCharsets.UTF_8);

		// 메시지를 DB에 저장
		ChatMessage savedMessage = chatMessageService.saveMessage(
			decodedGroupName,
			user.getId(),
			incomingMessage.content(),
			incomingMessage.type()
		);

		// DTO 변환 후 해당 채팅방 구독자에게 브로드캐스팅
		ChatMessageResBody chatMessageResBody = ChatMessageResBody.from(savedMessage);
		messagingTemplate.convertAndSend("/sub/chat/" + groupName, chatMessageResBody);
	}
}