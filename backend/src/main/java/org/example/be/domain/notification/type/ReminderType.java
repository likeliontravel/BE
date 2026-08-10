package org.example.be.domain.notification.type;

public enum ReminderType {

	D_1(NotificationType.SCHEDULE_D_1),
	D_DAY(NotificationType.SCHEDULE_D_DAY);

	private final NotificationType notificationType;

	ReminderType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

}
