package org.example.be.domain.notification.dto.response;

import java.util.List;

public record NotificationListResBody(
	List<NotificationResBody> notifications,
	Long nextCursor,
	boolean hasNext,
	long unreadCount
) {
	public static NotificationListResBody of(List<NotificationResBody> notifications, Long nextCursor, boolean hasNext,
		long unreadCount) {
		return new NotificationListResBody(notifications, nextCursor, hasNext, unreadCount);
	}
}
