package org.example.be.domain.chat.repository;

import static org.example.be.domain.chat.entity.QChatMessage.*;
import static org.example.be.domain.group.entity.QGroup.*;
import static org.example.be.domain.member.entity.QMember.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.chat.entity.QChatMessage;
import org.example.be.domain.chat.type.SearchDirection;
import org.example.be.domain.group.entity.Group;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<ChatMessage> findLatestMessagesForGroups(List<Group> groups) {
		if (groups == null || groups.isEmpty()) {
			return List.of();
		}

		QChatMessage subChatMessage = new QChatMessage("subChatMessage");

		return queryFactory
			.selectFrom(chatMessage)
			.join(chatMessage.group, group).fetchJoin()
			.join(chatMessage.sender, member).fetchJoin()
			.where(chatMessage.group.in(groups)
				.and(chatMessage.createdTime.eq(
					JPAExpressions
						.select(subChatMessage.createdTime.max())
						.from(subChatMessage)
						.where(subChatMessage.group.eq(chatMessage.group))
				)))
			.fetch();
	}

	@Override
	public List<ChatMessage> findRecentMessages(Group targetGroup, int limit) {
		return queryFactory
			.selectFrom(chatMessage)
			.join(chatMessage.group, group).fetchJoin()
			.join(chatMessage.sender, member).fetchJoin()
			.where(chatMessage.group.eq(targetGroup))
			.orderBy(chatMessage.createdTime.desc())
			.limit(limit)
			.fetch();
	}

	@Override
	public List<ChatMessage> findPreviousMessages(Group targetGroup, Long lastMessageId, int limit) {
		return queryFactory
			.selectFrom(chatMessage)
			.join(chatMessage.group, group).fetchJoin()
			.join(chatMessage.sender, member).fetchJoin()
			.where(
				chatMessage.group.eq(targetGroup),
				ltMessageId(lastMessageId)
			)
			.orderBy(chatMessage.createdTime.desc())
			.limit(limit)
			.fetch();
	}

	@Override
	public List<ChatMessage> searchMessagesWithKeyword(Group targetGroup, String keyword, Long lastMessageId,
		SearchDirection direction, int limit) {
		// BOTH: 검색 결과에서 특정 메시지를 클릭했을 때, 키워드와 무관하게 그 메시지를 중심으로
		// 이전/이후 메시지를 한 번에 가져와 채팅창을 그 위치로 점프시키기 위한 용도.
		if (direction == SearchDirection.BOTH) {
			List<ChatMessage> before = fetchDirectional(targetGroup, null, lastMessageId, SearchDirection.BEFORE,
				limit, false);
			List<ChatMessage> after = fetchDirectional(targetGroup, null, lastMessageId, SearchDirection.AFTER,
				limit, true);
			List<ChatMessage> combined = new ArrayList<>(before);
			combined.addAll(after);
			return combined;
		}
		return fetchDirectional(targetGroup, keyword, lastMessageId, direction, limit, false);
	}

	private List<ChatMessage> fetchDirectional(Group targetGroup, String keyword, Long lastMessageId,
		SearchDirection direction, int limit, boolean inclusive) {
		return queryFactory
			.selectFrom(chatMessage)
			.join(chatMessage.group, group).fetchJoin()
			.join(chatMessage.sender, member).fetchJoin()
			.where(
				chatMessage.group.eq(targetGroup),
				keywordCondition(keyword),
				cursorCondition(lastMessageId, direction, inclusive)
			)
			.orderBy(cursorOrder(direction))
			.limit(limit)
			.fetch();
	}

	// keyword가 없으면(BOTH 모드, 또는 일반 조회) 조건 없음
	private BooleanExpression keywordCondition(String keyword) {
		return (keyword == null || keyword.isBlank()) ? null : chatMessage.content.containsIgnoreCase(keyword);
	}

	// lastMessageId가 없으면(첫 페이지) 조건 없음, BEFORE면 과거, AFTER면 최신
	// inclusive는 BOTH 모드의 AFTER 쪽에서 클릭한 메시지 자신도 포함시키기 위함
	private BooleanExpression cursorCondition(Long lastMessageId, SearchDirection direction, boolean inclusive) {
		if (lastMessageId == null) {
			return null;
		}
		if (direction == SearchDirection.AFTER) {
			return inclusive ? chatMessage.id.goe(lastMessageId) : chatMessage.id.gt(lastMessageId);
		}
		return chatMessage.id.lt(lastMessageId);
	}

	// AFTER는 커서와 가장 가까운 최신 매치부터 limit개 뽑기 위해 오름차순, 그 외(BEFORE, 첫 페이지)는 내림차순
	private OrderSpecifier<Long> cursorOrder(SearchDirection direction) {
		return direction == SearchDirection.AFTER ? chatMessage.id.asc() : chatMessage.id.desc();
	}

	@Override
	public Optional<ChatMessage> findLatestMessage(Group targetGroup) {
		return Optional.ofNullable(
			queryFactory
				.selectFrom(chatMessage)
				.join(chatMessage.group, group).fetchJoin()
				.join(chatMessage.sender, member).fetchJoin()
				.where(chatMessage.group.eq(targetGroup))
				.orderBy(chatMessage.createdTime.desc())
				.fetchFirst() // limit(1).fetchOne() 과 동일
		);
	}

	private BooleanExpression ltMessageId(Long lastMessageId) {
		return lastMessageId == null ? null : chatMessage.id.lt(lastMessageId);
	}
}
