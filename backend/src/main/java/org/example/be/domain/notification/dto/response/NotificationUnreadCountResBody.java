package org.example.be.domain.notification.dto.response;

public record NotificationUnreadCountResBody(
	long unreadCount
) {
	public static NotificationUnreadCountResBody of(long unreadCount) {
		return new NotificationUnreadCountResBody(unreadCount);
	}
}
