package org.example.be.domain.notification.dto.response;

import java.time.LocalDateTime;

import org.example.be.domain.notification.entity.Notification;
import org.example.be.domain.notification.type.NotificationType;

public record NotificationResBody(
	Long id,
	NotificationType type,
	String message,
	Long targetId,
	String targetName,
	Long actorId,
	String actorName,
	String actorProfileImageUrl,
	Boolean read,
	LocalDateTime createdAt

) {
	public static NotificationResBody from(Notification notification) {
		boolean hasActor = notification.getActor() != null;
		return new NotificationResBody(
			notification.getId(),
			notification.getType(),
			notification.getMessage(),
			notification.getTargetId(),
			notification.getTargetName(),
			hasActor ? notification.getActor().getId() : null,
			hasActor ? notification.getActor().getName() : null,
			hasActor ? notification.getActor().getProfileImageUrl() : null,
			notification.getRead(),
			notification.getCreatedTime()
		);
	}
}
