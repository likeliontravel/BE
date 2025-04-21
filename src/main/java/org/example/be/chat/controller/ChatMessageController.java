package org.example.be.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.chat.dto.ChatMessageDTO;
import org.example.be.chat.entity.ChatMessage;
import org.example.be.chat.service.ChatMessageService;
import org.example.be.response.CommonResponse;
import org.example.be.security.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // 채팅방 최초 입장 시 최신 메시지 20개 조회
    @GetMapping("/{groupName}/messages")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getRecent20Messages(@PathVariable String groupName) {
        Map<String, Object> result = chatMessageService.getRecent20Messages(groupName);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "최근 메시지 20개 조회 성공"));
    }

    // 스크롤 업 시 과거 메시지 추가 20개 조회
    @GetMapping("/{groupName}/messages/prev")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getPrev20Messages(
            @PathVariable String groupName,
            @RequestParam Long lastMessageId
    ) {
        Map<String, Object> result = chatMessageService.getPrevious20Messages(groupName, lastMessageId);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "이전 메시지 추가 20개 조회 성공"));
    }

    // 키워드 기반 메시지 조회 ( 메시지 검색 )
    @GetMapping("/{groupName}/messages/search")
    public ResponseEntity<CommonResponse<Map<String, Object>>> searchMessages(
            @PathVariable String groupName,
            @RequestParam String keyword
    ) {
        Map<String, Object> result = chatMessageService.searchMessages(groupName, keyword);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "메시지 검색 성공"));
    }

    // 해당 그룹 가장 최신 메시지 1개 조회 ( 채팅페이지 내 그룹목록에 사용 )
    @GetMapping("/{groupName}/messages/latest")
    public ResponseEntity<CommonResponse<ChatMessageDTO>> getLatestMessage(
            @PathVariable String groupName
    ) {
        ChatMessageDTO result = chatMessageService.getLatestMessageOfGroup(groupName);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "해당 그룹 가장 최신 메시지 1개 조회 성공"));
    }

    // 메시지 저장
    // 웹소켓, STOMP 적용 전 테스트용
    @PostMapping("/{groupName}/messages/save")
    public ResponseEntity<CommonResponse<ChatMessageDTO>> saveMessage(
            @PathVariable String groupName,
            @RequestParam String content,
            @RequestParam String type
    ) {

        // 메시지 발신자는 요청자로 자동 설정
        String senderIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        ChatMessage savedChatMessage = chatMessageService.saveMessage(groupName, senderIdentifier, content, type);
        ChatMessageDTO result = chatMessageService.toDTO(savedChatMessage);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(result, "메시지 저장 성공"));

    }

    // 이미지 메시지 저장
    @PostMapping("/{groupName}/messages/image")
    public ResponseEntity<CommonResponse<ChatMessageDTO>> uploadImageMessage(
            @PathVariable String groupName,
            @RequestParam MultipartFile image
    ) throws IOException {
        String senderIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();

        ChatMessageDTO result = chatMessageService.saveImageMessage(groupName, senderIdentifier, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(result, "이미지 메시지 저장 성공"));
    }
}
