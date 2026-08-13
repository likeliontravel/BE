package org.example.be.domain.notification.scheduler;

import java.util.List;

import org.example.be.domain.notification.service.ScheduleReminderService;
import org.example.be.domain.schedule.entity.Schedule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleReminderScheduler {

	private final ScheduleReminderService scheduleReminderService;

	@Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
	public void sendScheduleReminders() {
		List<Schedule> targets = scheduleReminderService.findTargetSchedules();

		for (Schedule schedule : targets) {
			try {
				scheduleReminderService.processOne(schedule);
			} catch (Exception e) {
				log.error("[Notification] 일정 리마인더 처리 실패 - scheduleId={}", schedule.getId(), e);
			}
		}
	}
}
