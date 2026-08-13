package org.example.be.domain.notification.message;

import org.example.be.domain.notification.type.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessageFactory {

	private static final int MAX_ARG_LENGTH = 30;

	public String render(NotificationType type, String... args) {
		if (args.length != type.getArgCount()) {
			throw new IllegalArgumentException(type + "알림은 인자 " + type.getArgCount() + " 개가 필요합니다.");
		}
		String[] truncated = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			truncated[i] = truncate(args[i]);
		}
		return type.getTemplate().formatted((Object[])truncated);
	}

	private String truncate(String arg) {
		if (arg == null || arg.length() <= MAX_ARG_LENGTH) {
			return arg;
		}
		return arg.substring(0, MAX_ARG_LENGTH) + "...";
	}
}
