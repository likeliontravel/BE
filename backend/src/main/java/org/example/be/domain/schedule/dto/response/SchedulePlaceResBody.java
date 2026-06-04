package org.example.be.domain.schedule.dto.response;

import java.time.LocalDateTime;

import org.example.be.domain.place.shared.type.PlaceType;
import org.example.be.domain.schedule.entity.SchedulePlace;

public record SchedulePlaceResBody(
	Long id,
	String contentId,
	String title,
	String img,
	String address,
	PlaceType placeType,
	LocalDateTime visitStart,
	LocalDateTime visitedEnd,
	Integer dayOrder,
	Integer orderInDay
) {

	public static SchedulePlaceResBody from(SchedulePlace schedulePlace, PlaceSimpleResBody resBody) {
		return new SchedulePlaceResBody(
			schedulePlace.getId(),
			schedulePlace.getContentId(),
			resBody != null ? resBody.title() : "장소 정보를 불러올 수 없습니다.",
			resBody != null ? resBody.img() : null,
			resBody != null ? resBody.address() : "",
			schedulePlace.getPlaceType(),
			schedulePlace.getVisitStart(),
			schedulePlace.getVisitedEnd(),
			schedulePlace.getDayOrder(),
			schedulePlace.getOrderInDay()
		);
	}
}
