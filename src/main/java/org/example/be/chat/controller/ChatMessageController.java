package org.example.be.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.chat.dto.ChatMessageDTO;
import org.example.be.chat.entity.ChatMessage;
import org.example.be.chat.service.ChatMessageService;
import org.example.be.resolver.DecodedPathVariable;
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

    // 해당 그룹 채팅방 최신 20개 메시지 조회 ( 채팅방 최초 입장 시 호출용 )
    @GetMapping("/{groupName}/messages")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getRecent20Messages(
            @DecodedPathVariable String groupName
    ) {
        try {
            Map<String, Object> result = chatMessageService.getRecent20Messages(groupName);
            return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "최근 메시지 20개 조회 성공"));
        } catch (Exception e) {
            System.out.println("[Controller] 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(500, "서버 내부 오류: " + e.getMessage()));
        }
    }

    // 해당 메시지 기준 과거 20개 메시지 추가 조회 ( 스크롤 업 시 호출용 )
    @GetMapping("/{groupName}/messages/prev")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getPrev20Messages(
            @DecodedPathVariable String groupName,
            @RequestParam Long lastMessageId
    ) {
        Map<String, Object> result = chatMessageService.getPrevious20Messages(groupName, lastMessageId);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "이전 메시지 추가 20개 조회 성공"));
    }

    // 키워드 기반 메시지 조회 ( 메시지 검색 )
    @GetMapping("/{groupName}/messages/search")
    public ResponseEntity<CommonResponse<Map<String, Object>>> searchMessages(
            @DecodedPathVariable String groupName,
            @RequestParam String keyword
    ) {
        Map<String, Object> result = chatMessageService.searchMessages(groupName, keyword);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "메시지 검색 성공"));
    }

    // 해당 그룹의 가장 최신 메시지 1개 조회 ( 채팅 목록에서 요청용 )
    @GetMapping("/{groupName}/messages/latest")
    public ResponseEntity<CommonResponse<ChatMessageDTO>> getLatestMessage(
            @DecodedPathVariable String groupName
    ) {
        ChatMessageDTO result = chatMessageService.getLatestMessageOfGroup(groupName);
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "해당 그룹 가장 최신 메시지 조회 성공"));
    }

    // 이미지 메시지 업로드 ( REST 방식 - 미리보기를 띄우기 위해 publicURL을 먼저 보여줄 때 호출 )
    // GCS에 업로드 후 이미지의 public URL을 반환해준다
    @PostMapping("/image/upload")
    public ResponseEntity<CommonResponse<String>> uploadImageMessage(
            @RequestParam String groupName,
            @RequestParam MultipartFile image
    ) {
        String publicUrl = chatMessageService.uploadAndGetPreview(image, groupName);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(publicUrl, "이미지 메시지 저장 성공"));
    }


    // -----------------------------------------------------------------------------------------
    // 아래는 웹소켓 메시지핸들러 없이 REST API형식으로 작동시킬 수 있는 엔드포인트 정의입니다.
    // 실서비스에서 사용하지 않아 임시 주석처리합니다.

    // 텍스트 메시지 저장 - 테스트용 (웹소켓 미사용 시 REST로 전송)
//    @PostMapping("/{groupName}/messages/save")
//    public ResponseEntity<CommonResponse<ChatMessageDTO>> saveMessage(
//            @DecodedPathVariable String groupName,
//            @RequestParam String content,
//            @RequestParam String type
//    ) {
//        ChatMessage saved = chatMessageService.saveMessage(groupName, content, type);
//        ChatMessageDTO dto = chatMessageService.toDTO(saved);
//        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(dto, "메시지 저장 성공"));
//    }

    // ------------------------------------------------------------------------------------------
}
