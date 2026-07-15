package org.example.be.domain.schedule.dto.response;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.example.be.domain.schedule.entity.Schedule;

public record NearestScheduleResBody(
	String groupName,
	LocalDate startSchedule,
	long dDay
) {
	public static NearestScheduleResBody from(Schedule schedule, LocalDate today) {
		LocalDate startSchedule = schedule.getStartSchedule().toLocalDate();

		return new NearestScheduleResBody(
			schedule.getGroup().getGroupName(),
			startSchedule,
			ChronoUnit.DAYS.between(today, startSchedule)
		);
	}
}
