package org.example.be.domain.chat.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.be.domain.chat.dto.ChatMessageResBody;
import org.example.be.domain.chat.dto.ChatRoomListWithLatestMessageResBody;
import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.chat.repository.ChatMessageRepository;
import org.example.be.domain.chat.type.MessageType;
import org.example.be.domain.group.entity.Group;
import org.example.be.domain.group.repository.GroupRepository;
import org.example.be.domain.member.dto.response.MemberDto;
import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.repository.MemberRepository;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.storage.gcs.GCSService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final GroupRepository groupRepository;
	private final GCSService gcsService;
	private final MemberRepository memberRepository;

	// ==================== 일반 REST API ====================

	// 해당 그룹 가장 최신 메시지 20개 조회 ( 채팅방 최초 입장 시 호출용 )
	@Transactional
	public Map<String, Object> getRecent20Messages(String groupName, Long memberId) {
		log.debug("[Controller] 호출 시점 Authentication: " + SecurityContextHolder.getContext().getAuthentication());
		Group group = findGroupAndValidateMember(groupName, memberId);

		List<ChatMessage> messages = chatMessageRepository.findTop20ByGroupOrderByCreatedTimeDesc(group);
		if (messages.isEmpty()) {
			throw new BusinessException(ErrorCode.GROUP_CHAT_NOT_FOUND, "groupName: " + groupName);
		}
		return buildMessageWithProfiles(messages);
	}

	// 이전 메시지 20개 추가 조회 ( 스크롤 업 시 호출용 )
	@Transactional
	public Map<String, Object> getPrevious20Messages(String groupName, Long lastMessageId, Long memberId) {
		Group group = findGroupAndValidateMember(groupName, memberId);

		List<ChatMessage> messages = chatMessageRepository.findTop20ByGroupAndIdLessThanOrderByCreatedTimeDesc(group,
			lastMessageId);
		if (messages.isEmpty()) {
			throw new BusinessException(ErrorCode.CHAT_PREVIOUS_MESSAGE_NOT_FOUND,
				"groupName: " + groupName + ", messageId: " + lastMessageId);
		}
		return buildMessageWithProfiles(messages);
	}

	// 키워드 기반 메시지 검색
	@Transactional
	public Map<String, Object> searchMessages(String groupName, String keyword, Long memberId) {
		Group group = findGroupAndValidateMember(groupName, memberId);

		List<ChatMessage> messages = chatMessageRepository.findByGroupAndContentContainingIgnoreCaseOrderByCreatedTimeDesc(
			group, keyword);

		return buildMessageWithProfiles(messages);
	}

	// 해당 그룹 가장 마지막 메시지 조회 ( 그룹 채팅방 목록에서 표시용 )
	@Transactional
	public ChatMessageResBody getLatestMessageOfGroup(String groupName, Long memberId) {
		Group group = findGroupAndValidateMember(groupName, memberId);
		return chatMessageRepository.findTop1ByGroupOrderByCreatedTimeDesc(group)
			.map(ChatMessageResBody::from)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_CHAT_NOT_FOUND, "groupName: " + groupName));
	}

	// 사용자가 가입한 모든 그룹 + 각 그룹의 최신 메시지 1개를 한 번에 조회
	@Transactional(readOnly = true)
	public List<ChatRoomListWithLatestMessageResBody> getGroupsWithLatestMessage(Long memberId) {
		// Chat 도메인 마이그레이션 시 userIdentifier 관련 전부 없앨 예정( 임시 컴파일 오류 방지용 땜빵만 놓습니다. 리팩토링할 때 멤버로 바꿔용 )
		// 현재처럼 두면 최근 member도입 이후 가입한 회원에 대해서 userIdentifier라는걸 인식 못해서 아마 안될겁니다.
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "userIdentifier: " + memberId));

		// 요청자가 속한 그룹 목록
		List<Group> groups = groupRepository.findByMembersContaining(member);
		if (groups.isEmpty()) {
			return Collections.emptyList();
		}

		List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesForGroups(groups);

		// groupName -> latest ChatMessage 메핑
		Map<Long, ChatMessage> latestMessageMap =
			latestMessages.stream()
				.collect(Collectors.toMap(
					m -> m.getGroup().getId(),
					m -> m
				));

		// DTO로 변환
		List<ChatRoomListWithLatestMessageResBody> dtoList = groups.stream()
			.map(group -> {
				ChatMessage latest = latestMessageMap.get(group.getId());
				return ChatRoomListWithLatestMessageResBody.from(
					group.getGroupName(),
					latest != null ? latest.getContent() : null,
					latest != null ? latest.getCreatedTime() : null,
					latest != null ? latest.getType() : null
				);
			})
			.sorted(Comparator.comparing(ChatRoomListWithLatestMessageResBody::sendAt,
				Comparator.nullsLast(Comparator.naturalOrder())).reversed())
			.collect(Collectors.toList());

		return dtoList;

	}

	// ==================== 메시지 저장 관련 ====================

	// GCS에 이미지 업로드 수행, public URL 반환
	public String uploadAndGetPreview(MultipartFile image, String groupName, Long memberId) {
		String senderIdentifier = String.valueOf(memberId);
		return gcsService.uploadChatImage(image, senderIdentifier, groupName);
	}

	// 메시지 저장 ( TEXT / IMAGE ) - WebSocket에서 호출
	@Transactional
	public ChatMessage saveMessage(String groupName, Long memberId, String content, MessageType type) {
		Group group = findGroupAndValidateMember(groupName, memberId);
		Member sender = findMember(memberId);

		ChatMessage chatMessage = ChatMessage.builder()
			.group(group)
			.sender(sender)
			.content(content)
			.type(type)
			.build();
		try {
			return chatMessageRepository.save(chatMessage);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_CREATION_FAILED, "메시지 저장 실패 - message: " + e.getMessage());
		}

	}

	// ==================== 내부 사용 메서드 ====================

	// 그룹 존재 여부와 요청자가 그룹 내 멤버인지 검증하는 메서드
	private Group findGroupAndValidateMember(String groupName, Long memberId) {
		Group group = groupRepository.findByGroupName(groupName)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND, "groupName: " + groupName));

		log.debug("[ChatMessageService] 멤버 검증 시작 - 그룹: {}, 멤버ID: {}", groupName, memberId);

		if (!groupRepository.existsByGroupNameAndMembers_Id(groupName, memberId)) {
			throw new BusinessException(ErrorCode.GROUP_MEMBER_NOT_FOUND,
				"groupName: " + groupName + ", memberId: " + memberId);
		}
		return group;
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "memberId: " + memberId));
	}

	// 최종 반환해줄 메시지를 전송자의 프로필정보를 함께 담아 빌드해주는 메서드.
	private Map<String, Object> buildMessageWithProfiles(List<ChatMessage> messages) {
		List<ChatMessageResBody> dtoList = messages.stream()
			.sorted(Comparator.comparing(ChatMessage::getCreatedTime))
			.map(this::toDTO)
			.collect(Collectors.toList());

		Map<Long, MemberDto> profiles = messages.stream()
			.map(ChatMessage::getSender)
			.distinct()
			.collect(Collectors.toMap(
				Member::getId,
				member -> MemberDto.from(member, false)
			));

		Map<String, Object> result = new HashMap<>();
		result.put("messages", dtoList);
		result.put("senderProfiles", profiles);

		return result;
	}

	// Entity -> DTO 파싱
	public ChatMessageResBody toDTO(ChatMessage entity) {
		return ChatMessageResBody.builder()
			.id(entity.getId())
			.groupName(entity.getGroup().getGroupName())
			.senderId(entity.getSender().getId())
			.type(entity.getType())
			.content(entity.getContent())
			.sendAt(entity.getCreatedTime())
			.build();
	}

}