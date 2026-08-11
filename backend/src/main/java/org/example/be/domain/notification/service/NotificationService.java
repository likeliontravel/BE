package org.example.be.domain.notification.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.example.be.domain.notification.dto.response.NotificationListResBody;
import org.example.be.domain.notification.dto.response.NotificationResBody;
import org.example.be.domain.notification.dto.response.NotificationUnreadCountResBody;
import org.example.be.domain.notification.entity.Notification;
import org.example.be.domain.notification.repository.NotificationRepository;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final Clock clock;

	@Transactional(readOnly = true)
	public NotificationListResBody getNotifications(Long memberId, Long lastNotificationId, int size) {
		List<Notification> fetched = notificationRepository.findByReceiverIdWithCursor(memberId, lastNotificationId,
			size);

		boolean hasNext = fetched.size() > size;
		List<Notification> content = hasNext ? fetched.subList(0, size) : fetched;

		List<NotificationResBody> notifications = content.stream()
			.map(NotificationResBody::from)
			.toList();

		Long nextCursor = content.isEmpty() ? null : content.get(content.size() - 1).getId();
		long unreadCount = notificationRepository.countByReceiverIdAndReadFalse(memberId);

		return NotificationListResBody.of(notifications, nextCursor, hasNext, unreadCount);
	}

	@Transactional
	public void markAsRead(Long notificationId, Long memberId) {
		Notification notification = findByIdAndValidateOwner(notificationId, memberId);
		notification.markAsRead(LocalDateTime.now(clock));
	}

	@Transactional(readOnly = true)
	public NotificationUnreadCountResBody getUnreadCount(Long memberId) {
		return NotificationUnreadCountResBody.of(notificationRepository.countByReceiverIdAndReadFalse(memberId));
	}

	@Transactional
	public int markAllAsRead(Long memberId) {
		return notificationRepository.markAllAsRead(memberId, LocalDateTime.now(clock));

	}

	@Transactional
	public void deleteNotification(Long notificationId, Long memberId) {
		Notification notification = findByIdAndValidateOwner(notificationId, memberId);
		notificationRepository.delete(notification);
	}

	private Notification findByIdAndValidateOwner(Long notificationId, Long memberId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND,
				"notificationId: " + notificationId));

		if (!notification.getReceiver().getId().equals(memberId)) {
			throw new BusinessException(ErrorCode.NOTIFICATION_FORBIDDEN,
				"notificationId: " + notificationId + ", memberId: " + memberId);
		}
		return notification;
	}

}
