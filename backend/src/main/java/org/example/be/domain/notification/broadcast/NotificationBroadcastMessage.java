package org.example.be.domain.notification.broadcast;

import org.example.be.domain.notification.dto.response.NotificationResBody;

public record NotificationBroadcastMessage(
	Long receiverId,
	NotificationResBody payload
) {
}
