package org.example.be.chat.controller;

import java.util.Map;

import org.example.be.chat.dto.ChatMessageReqBody;
import org.example.be.chat.service.ChatMessageService;
import org.example.be.resolver.DecodedPathVariable;
import org.example.be.response.CommonResponse;
import org.example.be.security.config.SecurityUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	// 해당 그룹 채팅방 최신 20개 메시지 조회 ( 채팅방 최초 입장 시 호출용 )
	@GetMapping("/{groupName}/messages")
	public ResponseEntity<CommonResponse<Map<String, Object>>> getRecent20Messages(
		@DecodedPathVariable String groupName, @AuthenticationPrincipal SecurityUser securityUser
	) {
		Map<String, Object> result = chatMessageService.getRecent20Messages(groupName, securityUser);
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "최근 메시지 20개 조회 성공"));
	}

	// 해당 메시지 기준 과거 20개 메시지 추가 조회 ( 스크롤 업 시 호출용 )
	@GetMapping("/{groupName}/messages/prev")
	public ResponseEntity<CommonResponse<Map<String, Object>>> getPrev20Messages(
		@DecodedPathVariable String groupName,
		@RequestParam Long lastMessageId,
		@AuthenticationPrincipal SecurityUser securityUser
	) {
		Map<String, Object> result = chatMessageService.getPrevious20Messages(groupName, lastMessageId, securityUser);
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "이전 메시지 추가 20개 조회 성공"));
	}

	// 키워드 기반 메시지 조회 ( 메시지 검색 )
	@GetMapping("/{groupName}/messages/search")
	public ResponseEntity<CommonResponse<Map<String, Object>>> searchMessages(
		@DecodedPathVariable String groupName,
		@RequestParam String keyword,
		@AuthenticationPrincipal SecurityUser securityUser
	) {
		Map<String, Object> result = chatMessageService.searchMessages(groupName, keyword, securityUser);
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "메시지 검색 성공"));
	}

	// 해당 그룹의 가장 최신 메시지 1개 조회 ( 채팅 목록에서 요청용 )
	@GetMapping("/{groupName}/messages/latest")
	public ResponseEntity<CommonResponse<ChatMessageReqBody>> getLatestMessage(
		@DecodedPathVariable String groupName,
		@AuthenticationPrincipal SecurityUser securityUser
	) {
		ChatMessageReqBody result = chatMessageService.getLatestMessageOfGroup(groupName, securityUser);
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(result, "해당 그룹 가장 최신 메시지 조회 성공"));
	}

	// 채팅방 목록 조회 ( 최근 메시지 1개 포함 )
	// @GetMapping("/user-groups/with-latest")
	// public ResponseEntity<CommonResponse<List<ChatRoomListWithLatestMessageDTO>>> getGroupsWithLatestMessages(
	// 	@AuthenticationPrincipal SecurityUser securityUser) {
	// 	List<ChatRoomListWithLatestMessageDTO> groupsAndMessages = chatMessageService.getGroupsWithLatestMessage(
	// 		securityUser);
	// 	return ResponseEntity.status(HttpStatus.OK)
	// 		.body(CommonResponse.success(groupsAndMessages, "그룹 목록 및 각 최신 메시지 조회 성공"));
	// }

	// 이미지 메시지 업로드 ( REST 방식 - 미리보기를 띄우기 위해 publicURL을 먼저 보여줄 때 호출 )
	// GCS에 업로드 후 이미지의 public URL을 반환해준다
	@PostMapping("/image/upload")
	public ResponseEntity<CommonResponse<String>> uploadImageMessage(
		@RequestParam String groupName,
		@RequestParam MultipartFile image,
		@AuthenticationPrincipal SecurityUser securityUser
	) {
		String publicUrl = chatMessageService.uploadAndGetPreview(image, groupName, securityUser);
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
