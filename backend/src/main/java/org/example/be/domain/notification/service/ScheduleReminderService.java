package org.example.be.domain.notification.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.be.domain.group.entity.Group;
import org.example.be.domain.member.entity.Member;
import org.example.be.domain.notification.entity.ScheduleReminderLog;
import org.example.be.domain.notification.event.NotificationEvent;
import org.example.be.domain.notification.event.NotificationEventPublisher;
import org.example.be.domain.notification.message.NotificationMessageFactory;
import org.example.be.domain.notification.repository.ScheduleReminderLogRepository;
import org.example.be.domain.notification.type.ReminderType;
import org.example.be.domain.schedule.entity.Schedule;
import org.example.be.domain.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleReminderService {

	private final ScheduleRepository scheduleRepository;
	private final ScheduleReminderLogRepository scheduleReminderLogRepository;
	private final NotificationEventPublisher notificationEventPublisher;
	private final NotificationMessageFactory messageFactory;
	private final Clock clock;

	@Transactional(readOnly = true)
	public List<Schedule> findTargetSchedules() {
		LocalDate today = LocalDate.now(clock);
		LocalDateTime from = today.atStartOfDay();
		LocalDateTime to = today.plusDays(2).atStartOfDay();
		return scheduleRepository.findAllStartingBetweenWithMembers(from, to);
	}

	@Transactional
	public void processOne(Schedule schedule) {
		LocalDate today = LocalDate.now(clock);
		LocalDate startDate = schedule.getStartSchedule().toLocalDate();
		ReminderType reminderType = startDate.equals(today) ? ReminderType.D_DAY : ReminderType.D_1;

		if (scheduleReminderLogRepository.existsByScheduleIdAndReminderTypeAndReminderDate(
			schedule.getId(), reminderType, startDate)) {
			return;
		}

		Group group = schedule.getGroup();
		List<Long> receiverIds = group.getMembers().stream().map(Member::getId).toList();
		if (receiverIds.isEmpty()) {
			return;
		}

		String message = messageFactory.render(reminderType.getNotificationType(), group.getGroupName());

		notificationEventPublisher.publish(
			NotificationEvent.ofMany(receiverIds, null, reminderType.getNotificationType(),
				schedule.getId(), group.getGroupName(), message));

		scheduleReminderLogRepository.save(ScheduleReminderLog.create(schedule.getId(), reminderType, startDate));
	}
}
