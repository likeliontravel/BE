package org.example.be.schedule.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleReqBody(
	LocalDateTime startSchedule,
	LocalDateTime endSchedule,
	String groupName,
	List<SchedulePlaceReqBody> schedulePlaces
) {
}
