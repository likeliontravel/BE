package org.example.be.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.be.chat.dto.ChatMessageDTO;
import org.example.be.chat.entity.ChatMessage;
import org.example.be.chat.repository.ChatMessageRepository;
import org.example.be.gcs.GCSService;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.unifieduser.dto.UnifiedUsersNameAndProfileImageUrl;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// 채팅 메시지 관리 비즈니스
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final GroupRepository groupRepository;
    private final UnifiedUserRepository unifiedUserRepository;
    private final UnifiedUserService unifiedUserService;
    private final GCSService gcsService;

    // 그룹 채팅 페이지 진입 시, 최근 20개 메시지 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getRecent20Messages(String groupName) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다. groupName: " + groupName));

        List<ChatMessage> messages = chatMessageRepository.findTop20ByGroupOrderBySendAtDesc(group);
        List<ChatMessageDTO> messageDTOs = messages.stream()
                .sorted(Comparator.comparing(ChatMessage::getSendAt))
                .map(this::toDTO)
                .collect(Collectors.toList());

        return buildMessageWithProfiles(messageDTOs);
    }

    // 무한스크롤 - 마지막 메시지 id 기준 이전 메시지 20개 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getPrevious20Messages(String groupName, Long lastMessageId) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다. groupName: " + groupName));

        List<ChatMessage> messages = chatMessageRepository
                .findTop20ByGroupAndIdLessThanOrderBySendAtDesc(group, lastMessageId);

        List<ChatMessageDTO> messageDTOs = messages.stream()
                .sorted(Comparator.comparing(ChatMessage::getSendAt))
                .map(this::toDTO)
                .collect(Collectors.toList());

        return buildMessageWithProfiles(messageDTOs);
    }


    // 메시지 저장하기 ( TEXT / IMAGE - 테스트용 메서드)
    @Transactional
    public ChatMessage saveMessage(String groupName, String senderIdentifier, String content, String type) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹이 존재하지 않습니다. groupName: " + groupName));
        UnifiedUser sender = unifiedUserRepository.findByUserIdentifier(senderIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 userIdentifier의 유저를 찾을 수 없습니다. userIdentifier: " + senderIdentifier));

        ChatMessage.MessageType messageType;
        try {
            messageType = ChatMessage.MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 메시지 타입입니다. ( TEXT / IMAGE )");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .group(group)
                .sender(sender)
                .content(content)
                .type(messageType)
                .sendAt(LocalDateTime.now())
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    // 키워드로 메시지 검색
    @Transactional(readOnly = true)
    public Map<String, Object> searchMessages(String groupName, String keyword) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다. groupName: " + groupName));

        List<ChatMessage> messages = chatMessageRepository.findByGroupAndContentContainingIgnoreCaseOrderBySendAtDesc(group, keyword);

        List<ChatMessageDTO> messageDTOs = messages.stream()
                .sorted(Comparator.comparing(ChatMessage::getSendAt))
                .map(this::toDTO)
                .collect(Collectors.toList());

        return buildMessageWithProfiles(messageDTOs);


    }

    // 채팅방 페이지 내 그룹 목록용 그룹별 가장 최근 메시지 1개 조회
    @Transactional(readOnly = true)
    public ChatMessageDTO getLatestMessageOfGroup(String groupName) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다. groupName: " + groupName));

        ChatMessage message = chatMessageRepository.findTop1ByGroupOrderBySendAtDesc(group);
        return toDTO(message);
    }

    // DTO 변환
    public ChatMessageDTO toDTO(ChatMessage entity) {
        return ChatMessageDTO.builder()
                .id(entity.getId())
                .groupName(entity.getGroup().getGroupName())
                .senderIdentifier(entity.getSender().getUserIdentifier())
                .type(entity.getType().name())
                .content(entity.getContent())
                .sendAt(entity.getSendAt())
                .build();
    }

    /**
     * DTO List + 유저 정보 맵핑해서 반환
     * @param: 변환된 dto 리스트
     * @return: 메시지 객체 + 메시지들에 중복제거된 identifier기준 유저의 이름과 프로필사진url
     */
    private Map<String, Object> buildMessageWithProfiles(List<ChatMessageDTO> messages) {
        // 입력받은 메시지들에서 identifier만 추출해 중복 제거
        Set<String> senderIdentifiers = messages.stream()
                .map(ChatMessageDTO::getSenderIdentifier)
                .collect(Collectors.toSet());

        // identifier와 유저이름/프로필사진url 객체 Map으로 저장
        Map<String, UnifiedUsersNameAndProfileImageUrl> senderProfiles = new HashMap<>();
        for (String senderIdentifier : senderIdentifiers) {
            senderProfiles.put(senderIdentifier, unifiedUserService.getNameAndProfileImageUrlByUserIdentifier(senderIdentifier));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);
        response.put("senderProfiles", senderProfiles);

        return response;

    }

    // 이미지 메시지 저장
    @Transactional
    public ChatMessageDTO saveImageMessage(String groupName, String senderIdentifier, MultipartFile image) throws IOException {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 그룹을 찾을 수 없습니다. groupName: " + groupName));
        UnifiedUser sender = unifiedUserRepository.findByUserIdentifier(senderIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. userIdentifier: " + senderIdentifier));

        // GCS에 이미지 업로드
        String imageUrl = gcsService.uploadChatImage(image, senderIdentifier, groupName);

        ChatMessage chatMessage = ChatMessage.builder()
                .group(group)
                .sender(sender)
                .type(ChatMessage.MessageType.IMAGE)
                .content(imageUrl)
                .sendAt(LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(chatMessage);
        return toDTO(saved);
    }

}
