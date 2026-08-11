package org.example.be.domain.notification.repository;

import java.util.List;

import org.example.be.domain.notification.entity.Notification;

public interface NotificationRepositoryCustom {

	//알림 목록 커서 조회, lastNotificationId가 null이면 첫 페이지
	List<Notification> findByReceiverIdWithCursor(Long receiverId, Long lastNotificationId, int size);
}
