package org.example.be.schedule.dto.response;

import java.time.LocalDateTime;

import org.example.be.place.entity.PlaceType;
import org.example.be.schedule.entity.SchedulePlace;

public record SchedulePlaceResBody(
	Long id,
	String contentId,
	PlaceType placeType,
	LocalDateTime visitStart,
	LocalDateTime visitedEnd, Integer dayOrder,
	Integer orderInDay
) {
	public static SchedulePlaceResBody from(SchedulePlace schedulePlace) {
		return new SchedulePlaceResBody(
			schedulePlace.getId(),
			schedulePlace.getContentId(),
			schedulePlace.getPlaceType(),
			schedulePlace.getVisitStart(),
			schedulePlace.getVisitedEnd(),
			schedulePlace.getDayOrder(),
			schedulePlace.getOrderInDay()
		);
	}
}
