package org.example.be.domain.schedule.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.example.be.domain.schedule.entity.Schedule;

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
				.map(place -> SchedulePlaceResBody.from(place, null))
				.toList()
		);
	}
	
	public static ScheduleResBody from(Schedule schedule, Map<String, String> placeTitles) {
		return new ScheduleResBody(
			schedule.getId(),
			schedule.getStartSchedule(),
			schedule.getEndSchedule(),
			schedule.getGroup().getGroupName(),
			schedule.getSchedulePlaces().stream()
				.map(place -> SchedulePlaceResBody.from(place, placeTitles.get(place.getContentId())))
				.toList()
		);
	}
}
