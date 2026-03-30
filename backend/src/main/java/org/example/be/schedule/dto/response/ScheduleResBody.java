package org.example.be.schedule.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.example.be.schedule.entity.Schedule;

public record ScheduleResBody(
	Long scheduleId,
	LocalDateTime startSchedule,
	LocalDateTime endSchedule,
	String groupName,
	List<SchedulePlaceResBody> schedulePlaces
) {
	public static ScheduleResBody from(Schedule schedule) {
		return new ScheduleResBody(
			schedule.getId(),
			schedule.getStartSchedule(),
			schedule.getEndSchedule(),
			schedule.getGroup().getGroupName(),
			schedule.getSchedulePlaces().stream()
				.map(SchedulePlaceResBody::from)
				.toList()
		);
	}
}
