package org.example.be.domain.notification.repository;

import static org.example.be.domain.member.entity.QMember.*;
import static org.example.be.domain.notification.entity.QNotification.*;

import java.util.List;

import org.example.be.domain.notification.entity.Notification;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Notification> findByReceiverIdWithCursor(Long receiverId, Long lastNotificationId, int size) {
		return queryFactory
			.selectFrom(notification)
			.leftJoin(notification.actor, member).fetchJoin()
			.where(notification.receiver.id.eq(receiverId),
				ltNotificationId(lastNotificationId)
			)
			.orderBy(notification.id.desc())
			.limit(size + 1)
			.fetch();
	}

	private BooleanExpression ltNotificationId(Long lastNotificationId) {
		return lastNotificationId == null ? null : notification.id.lt(lastNotificationId);
	}
}
