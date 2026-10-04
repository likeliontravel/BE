package org.example.be.domain.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.be.domain.chat.dto.ChatMessageResBody;
import org.example.be.domain.chat.repository.ChatMessageRepository;
import org.example.be.domain.group.entity.Group;
import org.example.be.domain.group.repository.GroupRepository;
import org.example.be.domain.member.repository.MemberRepository;
import org.example.be.storage.gcs.GCSService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// ChatMessageService 빈 결과 응답 단위 테스트 (Task 3-1 · A8)
// 빈 결과는 오류가 아니므로 예외(204) 대신 정상 값을 반환해야 한다.
// 특히 채팅 목록 2종은 프론트(useChat.ts)가 data 를 { senderProfiles, messages } 로 구조분해하므로
// 빈 결과에서도 두 키가 반드시 존재해야 한다 — data 가 null 이나 [] 가 되면 프론트에서 TypeError 가 난다.
@DisplayName("ChatMessageService 빈 결과 응답 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

	private static final String GROUP_NAME = "빈방그룹";
	private static final Long MEMBER_ID = 1L;

	@Mock
	private ChatMessageRepository chatMessageRepository;
	@Mock
	private GroupRepository groupRepository;
	@Mock
	private GCSService gcsService;
	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private ChatMessageService chatMessageService;

	private Group group;

	@BeforeEach
	void setUp() {
		// Group 은 생성자가 protected 라 mock 으로 대체한다 (그룹 조회·멤버 검증 통과용)
		group = mock(Group.class);
		when(groupRepository.findByGroupName(GROUP_NAME)).thenReturn(Optional.of(group));
		when(groupRepository.existsByGroupNameAndMembers_Id(GROUP_NAME, MEMBER_ID)).thenReturn(true);
	}

	@Test
	@DisplayName("최근 메시지가 없으면 예외 없이 { messages: [], senderProfiles: {} } 를 반환한다")
	void getRecent20Messages_empty_returnsEmptyShape() {
		when(chatMessageRepository.findRecentMessages(group, 20)).thenReturn(List.of());

		Map<String, Object> result = chatMessageService.getRecent20Messages(GROUP_NAME, MEMBER_ID);

		assertEmptyMessageShape(result);
	}

	@Test
	@DisplayName("이전 메시지가 더 없으면 예외 없이 같은 빈 shape 를 반환한다")
	void getPrevious20Messages_empty_returnsEmptyShape() {
		Long lastMessageId = 100L;
		when(chatMessageRepository.findPreviousMessages(group, lastMessageId, 20)).thenReturn(List.of());

		Map<String, Object> result = chatMessageService.getPrevious20Messages(GROUP_NAME, lastMessageId, MEMBER_ID);

		assertEmptyMessageShape(result);
	}

	@Test
	@DisplayName("그룹에 메시지가 하나도 없으면 최신 메시지는 null 이다 (응답에서 data 키 생략)")
	void getLatestMessageOfGroup_empty_returnsNull() {
		when(chatMessageRepository.findLatestMessage(group)).thenReturn(Optional.empty());

		ChatMessageResBody result = chatMessageService.getLatestMessageOfGroup(GROUP_NAME, MEMBER_ID);

		assertThat(result).isNull();
	}

	// 프론트 구조분해 계약: 키 2개가 정확히 존재하고, 각각 빈 List / 빈 Map 이어야 한다 (null 아님)
	private void assertEmptyMessageShape(Map<String, Object> result) {
		assertThat(result).containsOnlyKeys("messages", "senderProfiles");
		assertThat(result.get("messages")).isInstanceOf(List.class);
		assertThat((List<?>)result.get("messages")).isEmpty();
		assertThat(result.get("senderProfiles")).isInstanceOf(Map.class);
		assertThat((Map<?, ?>)result.get("senderProfiles")).isEmpty();
	}
}