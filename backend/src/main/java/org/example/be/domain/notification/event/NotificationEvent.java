package org.example.be.domain.notification.event;

import java.util.List;

import org.example.be.domain.notification.type.NotificationType;

public record NotificationEvent(
	List<Long> receiverIds,
	Long actorId,
	NotificationType type,
	Long targetId,
	String targetName,
	String message
) {
	public static NotificationEvent of(Long receiverId, Long actorId, NotificationType type, Long targetId,
		String targetName, String message) {
		return new NotificationEvent(List.of(receiverId), actorId, type, targetId, targetName, message);
	}

	public static NotificationEvent ofMany(List<Long> receiverIds, Long actorId, NotificationType type, Long targetId,
		String targetName, String message) {
		return new NotificationEvent(receiverIds, actorId, type, targetId, targetName, message);
	}
}
