package org.example.be.domain.schedule.dto.response;

import java.time.LocalDateTime;

import org.example.be.domain.place.shared.type.PlaceType;
import org.example.be.domain.schedule.entity.SchedulePlace;

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
