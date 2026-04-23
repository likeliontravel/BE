package org.example.be.domain.chat.repository;

import static org.example.be.domain.chat.entity.QChatMessage.*;

import java.util.List;

import org.example.be.domain.chat.entity.ChatMessage;
import org.example.be.domain.chat.entity.QChatMessage;
import org.example.be.domain.group.entity.Group;

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
			.where(chatMessage.group.in(groups)
				.and(chatMessage.createdTime.eq(
					JPAExpressions
						.select(subChatMessage.createdTime.max())
						.from(subChatMessage)
						.where(subChatMessage.group.eq(chatMessage.group))
				)))
			.fetch();
	}
}
