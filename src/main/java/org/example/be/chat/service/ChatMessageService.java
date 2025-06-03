package org.example.be.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.be.chat.dto.ChatMessageDTO;
import org.example.be.chat.entity.ChatMessage;
import org.example.be.chat.repository.ChatMessageRepository;
import org.example.be.exception.custom.ForbiddenResourceAccessException;
import org.example.be.exception.custom.GCSUploadFailedException;
import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.gcs.GCSService;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.dto.UnifiedUsersNameAndProfileImageUrl;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
import org.example.be.unifieduser.service.UnifiedUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final GroupRepository groupRepository;
    private final UnifiedUserRepository unifiedUserRepository;
    private final UnifiedUserService unifiedUserService;
    private final GCSService gcsService;

    // ==================== 일반 REST API ====================

    // 해당 그룹 가장 최신 메시지 20개 조회 ( 채팅방 최초 입장 시 호출용 )
    @Transactional
    public Map<String, Object> getRecent20Messages(String groupName) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        System.out.println("[Controller] 호출 시점 Authentication: " + SecurityContextHolder.getContext().getAuthentication());
        Group group = findGroupAndValidateMember(groupName, userIdentifier);

        List<ChatMessage> messages = chatMessageRepository.findTop20ByGroupOrderBySendAtDesc(group);
        if (messages.isEmpty()) {
            throw new NoSuchElementException("해당 그룹에 아직 메시지가 존재하지 않습니다. groupName: " + groupName);
        }
        return buildMessageWithProfiles(messages);
    }

    // 이전 메시지 20개 추가 조회 ( 스크롤 업 시 호출용 )
    @Transactional
    public Map<String, Object> getPrevious20Messages(String groupName, Long lastMessageId) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        Group group = findGroupAndValidateMember(groupName, userIdentifier);

        List<ChatMessage> messages = chatMessageRepository.findTop20ByGroupAndIdLessThanOrderBySendAtDesc(group, lastMessageId);
        if (messages.isEmpty()) {
            throw new NoSuchElementException("해당 메시지 이전에 전송된 메시지가 없습니다. messageId: " + lastMessageId);
        }
        return buildMessageWithProfiles(messages);
    }

    // 키워드 기반 메시지 검색
    @Transactional
    public Map<String, Object> searchMessages(String groupName, String keyword) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        Group group = findGroupAndValidateMember(groupName, userIdentifier);

        List<ChatMessage> messages = chatMessageRepository.findByGroupAndContentContainingIgnoreCaseOrderBySendAtDesc(group, keyword);
        if (messages.isEmpty()) {
            throw new NoSuchElementException("해당 키워드로 검색된 결과가 없습니다. keyword: " + keyword);
        }
        return buildMessageWithProfiles(messages);
    }

    // 해당 그룹 가장 마지막 메시지 조회 ( 그룹 채팅방 목록에서 표시용 )
    @Transactional
    public ChatMessageDTO getLatestMessageOfGroup(String groupName) {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        Group group = findGroupAndValidateMember(groupName, userIdentifier);
        Optional<ChatMessage> message= chatMessageRepository.findTop1ByGroupOrderBySendAtDesc(group);
        if (message.isPresent()) {
            return toDTO(message.get());
        } else {
            throw new NoSuchElementException("해당 그룹의 메시지가 없습니다. groupName: " + groupName);
        }
    }

    // ==================== 메시지 저장 관련 ====================

    // GCS에 이미지 업로드 수행, public URL 반환
    public String uploadAndGetPreview(MultipartFile image, String groupName) {
        try {
            String senderIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
            validateImageFile(image);
            return gcsService.uploadChatImage(image, senderIdentifier, groupName);
        } catch (IOException e) {
            throw new GCSUploadFailedException("이미지 업로드 중 오류가 발생했습니다.", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    // 메시지 저장 ( TEXT / IMAGE ) - WebSocket에서 호출
    @Transactional
    public ChatMessage saveMessage(String groupName, String senderIdentifier, String content, String type) {
        Group group = findGroupAndValidateMember(groupName, senderIdentifier);
        UnifiedUser sender = findUser(senderIdentifier);

        // TEXT / IMAGE 결정
        ChatMessage.MessageType messageType;
        try {
            messageType = ChatMessage.MessageType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("지원하지 않는 메시지 타입입니다.");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .group(group)
                .sender(sender)
                .content(content)
                .type(messageType)
                .sendAt(LocalDateTime.now())
                .build();
        try {
            return chatMessageRepository.save(chatMessage);
        } catch (Exception e) {
            throw new ResourceCreationException("메시지 저장에 실패했습니다.");
        }


    }


    // ==================== 내부 사용 메서드 ====================
    // 들어온 MultipartFile이 이미지인지 검증하는 메서드 - 이미지가 아닐 경우 예외 발생
    private void validateImageFile(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

    // 그룹 존재 여부와 요청자가 그룹 내 멤버인지 검증하는 메서드
    private Group findGroupAndValidateMember(String groupName, String userIdentifier) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹을 찾을 수 없습니다. groupName: " + groupName));

        System.out.println("[ChatMessageService에서 검증 로그] 그룹 이름: " + groupName);
        System.out.println("[검증 로그] 요청자 userIdentifier: " + userIdentifier);
        System.out.println("[검증 로그] 그룹 멤버 목록:");
        group.getMembers().forEach(member ->
                System.out.println(" - " + member.getUserIdentifier())
        );

        boolean isMember = group.getMembers().stream()
                .anyMatch(user -> user.getUserIdentifier().equals(userIdentifier));

        if (!isMember) {
            throw new ForbiddenResourceAccessException("해당 그룹에 가입되어 있지 않습니다.");
        }
        return group;
    }

    // userIdentifier로 UnifiedUser 찾아 반환
    private UnifiedUser findUser(String userIdentifier) {
        return unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. userIdentifier: " + userIdentifier));
    }

    // 최종 반환해줄 메시지를 전송자의 프로필정보를 함께 담아 빌드해주는 메서드.
    private Map<String, Object> buildMessageWithProfiles(List<ChatMessage> messages) {
        List<ChatMessageDTO> dtoList = messages.stream()
                .sorted(Comparator.comparing(ChatMessage::getSendAt))
                .map(this::toDTO)
                .collect(Collectors.toList());

        Set<String> senderIdentifiers = dtoList.stream()
                .map(ChatMessageDTO::getSenderIdentifier)
                .collect(Collectors.toSet());

        Map<String, UnifiedUsersNameAndProfileImageUrl> profiles = new HashMap<>();
        for (String identifier : senderIdentifiers) {
            profiles.put(identifier, unifiedUserService.getNameAndProfileImageUrlByUserIdentifier(identifier));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("messages", dtoList);
        result.put("senderProfiles", profiles);

        return result;
    }

    // Entity -> DTO 파싱
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

}