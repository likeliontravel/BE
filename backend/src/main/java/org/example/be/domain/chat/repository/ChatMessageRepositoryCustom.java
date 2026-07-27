package org.example.be.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.chat.type.SearchDirection;
import org.example.be.domain.group.entity.Group;

public interface ChatMessageRepositoryCustom {
	List<ChatMessage> findLatestMessagesForGroups(List<Group> groups);

	// 채팅 페이지 입장 시 최근 메시지 조회 ( fetch join 적용 )
	List<ChatMessage> findRecentMessages(Group group, int limit);

	// 이전 메시지 추가 조회 ( 무한스크롤용, fetch join 적용 )
	List<ChatMessage> findPreviousMessages(Group group, Long lastMessageId, int limit);

	// 키워드 검색 ( 커서 기반 페이지네이션, direction으로 이전/다음 매치 조회, fetch join 적용 )
	// direction=BOTH면 keyword 무시하고 lastMessageId(클릭한 메시지) 기준 전후 메시지를 한 번에 조회
	List<ChatMessage> searchMessagesWithKeyword(Group group, String keyword, Long lastMessageId,
		SearchDirection direction, int limit);

	// 그룹별 최신 메시지 1개 조회 ( fetch join 적용 )
	Optional<ChatMessage> findLatestMessage(Group group);
}
