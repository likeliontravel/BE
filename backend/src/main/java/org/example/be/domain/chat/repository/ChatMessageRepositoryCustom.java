package org.example.be.domain.chat.repository;

import java.util.List;

import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.group.entity.Group;

public interface ChatMessageRepositoryCustom {
	List<ChatMessage> findLatestMessagesForGroups(List<Group> groups);

	// 채팅 페이지 입장 시 최근 메시지 조회 ( fetch join 적용 )
	List<ChatMessage> findRecentMessages(Group group, int limit);

	// 이전 메시지 추가 조회 ( 무한스크롤용, fetch join 적용 )
	List<ChatMessage> findPreviousMessages(Group group, Long lastMessageId, int limit);

	// 키워드 검색 ( fetch join 적용 )
	List<ChatMessage> searchMessagesWithKeyword(Group group, String keyword);
}
