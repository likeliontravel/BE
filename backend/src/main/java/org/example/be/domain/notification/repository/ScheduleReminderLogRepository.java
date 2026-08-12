package org.example.be.domain.notification.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.be.domain.notification.entity.ScheduleReminderLog;
import org.example.be.domain.notification.type.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleReminderLogRepository extends JpaRepository<ScheduleReminderLog, Long> {

	// 리마인더 중복 실행 방지용
	boolean existsByScheduleIdAndReminderTypeAndReminderDate(Long scheduleId, ReminderType reminderType,
		LocalDate reminderDate);

	// 30일 뒤 삭제용
	@Modifying
	@Query("DELETE FROM ScheduleReminderLog l WHERE l.createdTime < :threshold")
	int deleteByCreatedTimeBefore(@Param("threshold") LocalDateTime threshold);

}
