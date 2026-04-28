package org.example.be.domain.chat.repository;

import static org.example.be.domain.chat.entity.QChatMessage.*;
import static org.example.be.domain.group.entity.QGroup.*;
import static org.example.be.domain.member.entity.QMember.*;

import java.util.List;
import java.util.Optional;

import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.chat.entity.QChatMessage;
import org.example.be.domain.group.entity.Group;

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
	public List<ChatMessage> searchMessagesWithKeyword(Group targetGroup, String keyword) {
		return queryFactory
			.selectFrom(chatMessage)
			.join(chatMessage.group, group).fetchJoin()
			.join(chatMessage.sender, member).fetchJoin()
			.where(
				chatMessage.group.eq(targetGroup),
				chatMessage.content.containsIgnoreCase(keyword)
			)
			.orderBy(chatMessage.createdTime.desc())
			.fetch();
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
