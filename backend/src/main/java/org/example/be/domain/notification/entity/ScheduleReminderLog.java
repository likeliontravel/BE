package org.example.be.domain.notification.entity;

import java.time.LocalDate;

import org.example.be.domain.notification.type.ReminderType;
import org.example.be.global.entity.Base;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 스케줄 리마인더 중복 발송 방지용 로그. (schedule_id, reminder_type, reminder_date) 조합이 유니크 (V5 DDL에서 제약)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "schedule_reminder_log")
public class ScheduleReminderLog extends Base {

	@Column(name = "schedule_id", nullable = false)
	private Long scheduleId;

	@Enumerated(EnumType.STRING)
	@Column(name = "reminder_type", nullable = false, length = 20)
	private ReminderType reminderType;

	// 유니크 키에 포함되는 이유: 일정 시작일이 변경되면 새 날짜로 다시 리마인더를 보낼 수 있어야 하므로
	@Column(name = "reminder_date", nullable = false)
	private LocalDate reminderDate;

	public static ScheduleReminderLog create(Long scheduleId, ReminderType reminderType, LocalDate reminderDate) {
		return new ScheduleReminderLog(scheduleId, reminderType, reminderDate);
	}

}
