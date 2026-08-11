package org.example.be.domain.notification.repository;

import java.time.LocalDate;

import org.example.be.domain.notification.entity.ScheduleReminderLog;
import org.example.be.domain.notification.type.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleReminderLogRepository extends JpaRepository<ScheduleReminderLog, Long> {

	// 리마인더 중복 실행 방지용
	boolean existsByScheduleIdAndReminderTypeAndReminderDate(Long scheduleId, ReminderType reminderType,
		LocalDate reminderDate);

}
