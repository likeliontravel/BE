package org.example.be.schedule.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleResBody(
	Long scheduleId,
	LocalDateTime startSchedule,
	LocalDateTime endSchedule,
	String groupName,
	List<SchedulePlaceResBody> schedulePlaces
) {
}
