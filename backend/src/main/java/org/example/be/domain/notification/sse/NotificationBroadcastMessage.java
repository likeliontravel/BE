package org.example.be.domain.notification.sse;

import org.example.be.domain.notification.dto.response.NotificationResBody;

public record NotificationBroadcastMessage(
	Long receiverId,
	NotificationResBody payload
) {
}
