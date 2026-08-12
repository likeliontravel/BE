package org.example.be.domain.notification.event;

import org.example.be.domain.notification.service.NotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

	private final NotificationService notificationService;

	@Async("notificationTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(NotificationEvent event) {
		try {
			notificationService.createAndSend(event);
		} catch (Exception e) {
			log.error("[Notification] 알림 처리 실패 - type={}, targetId={}, receivers={}",
				event.type(), event.targetId(), event.receiverIds(), e);
		}
	}
}
