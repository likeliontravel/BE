package org.example.be.domain.chat.repository;

import static org.example.be.domain.chat.entity.QChatMessage.*;
import static org.example.be.domain.group.entity.QGroup.*;
import static org.example.be.domain.member.entity.QMember.*;

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
		return queryFactory
			.selectFrom(chatMessage)
			.join(chatMessage.group, group).fetchJoin()
			.join(chatMessage.sender, member).fetchJoin()
			.where(
				chatMessage.group.eq(targetGroup),
				chatMessage.content.containsIgnoreCase(keyword),
				cursorCondition(lastMessageId, direction)
			)
			.orderBy(cursorOrder(direction))
			.limit(limit)
			.fetch();
	}

	// lastMessageId가 없으면(첫 페이지) 조건 없음, BEFORE면 과거 매치, AFTER면 최신 매치
	private BooleanExpression cursorCondition(Long lastMessageId, SearchDirection direction) {
		if (lastMessageId == null) {
			return null;
		}
		return direction == SearchDirection.AFTER
			? chatMessage.id.gt(lastMessageId)
			: chatMessage.id.lt(lastMessageId);
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
