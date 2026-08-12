package org.example.be.domain.notification.scheduler;

import java.time.Clock;
import java.time.LocalDateTime;

import org.example.be.domain.notification.repository.NotificationRepository;
import org.example.be.domain.notification.repository.ScheduleReminderLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

	private static final int RETENTION_DAYS = 30;

	private final NotificationRepository notificationRepository;
	private final ScheduleReminderLogRepository scheduleReminderLogRepository;
	private final Clock clock;

	// 04:30 KST 고정 — 트래픽 최저 시간대 + TourDataScheduler(06:00 KST)와 겹치지 않도록 잡은 시간이니 변경 시 주의
	// 단일 DELETE문은 매칭 행 전부에 갭 락을 잡는다. 지금 규모에선 무해하지만
	// 일 알림량이 수만 건을 넘으면 LIMIT 1000 네이티브 쿼리 루프로 전환 필요 (idx_notification_created_at 인덱스로 전환 비용은 낮음)
	@Scheduled(cron = "0 30 4 * * *", zone = "Asia/Seoul")
	public void cleanupOldNotifications() {
		LocalDateTime threshold = LocalDateTime.now(clock).minusDays(RETENTION_DAYS);

		int deletedNotifications = notificationRepository.deleteByCreatedTimeBefore(threshold);
		int deletedLogs = scheduleReminderLogRepository.deleteByCreatedTimeBefore(threshold);

		log.info("[Notification] {}일 경과 데이터 정리 완료 - notifications={}, reminderLogs={}",
			RETENTION_DAYS, deletedNotifications, deletedLogs);
	}
}
