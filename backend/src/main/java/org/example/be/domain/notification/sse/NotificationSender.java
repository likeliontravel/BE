package org.example.be.domain.notification.sse;

import org.example.be.domain.notification.dto.response.NotificationResBody;

public interface NotificationSender {
	void send(Long receiverId, NotificationResBody payload);

}
