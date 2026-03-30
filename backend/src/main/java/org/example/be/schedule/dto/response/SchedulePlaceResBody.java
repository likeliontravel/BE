package org.example.be.schedule.dto.response;

import java.time.LocalDateTime;

import org.example.be.place.entity.PlaceType;

public record SchedulePlaceResBody(
	Long id,
	String contentId,
	PlaceType placeType,
	LocalDateTime visitStart,
	LocalDateTime visitedEnd, Integer dayOrder,
	Integer orderInDay
) {
}
