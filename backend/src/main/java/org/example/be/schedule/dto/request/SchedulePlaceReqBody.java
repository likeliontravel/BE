package org.example.be.schedule.dto.request;

import java.time.LocalDateTime;

import org.example.be.place.entity.PlaceType;

public record SchedulePlaceReqBody(
	Long scheduleId,
	String contentId,
	PlaceType placeType,
	LocalDateTime visitStart,
	LocalDateTime visitedEnd,
	Integer dayOrder,
	Integer orderInDay
) {
}
