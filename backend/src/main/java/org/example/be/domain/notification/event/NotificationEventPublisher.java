package org.example.be.domain.notification.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

	private final ApplicationEventPublisher applicationEventPublisher;

	public void publish(NotificationEvent event) {
		applicationEventPublisher.publishEvent(event);
	}
}
