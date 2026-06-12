package org.example.be.domain.schedule.dto.response;

import org.example.be.domain.place.shared.type.PlaceType;
import org.example.be.domain.schedule.entity.SchedulePlace;

// 세부 일정 전체 삭제 응답 List의 원소 ResBody
// 삭제 블록 하나의 자기 필드만 응답에 포함해 제공
// 응답은 List<SchedulePlaceDeleteResBody> 형태로 CommonResponse에 담겨 나간다.
public record SchedulePlaceDeleteResBody(
	Long schedulePlaceId,
	String contentId,
	PlaceType placeType,
	Integer dayOrder,
	Integer orderInDay
) {

	// 삭제 직전의 SchedulePlace 엔티티에서 필드만
	public static SchedulePlaceDeleteResBody from(SchedulePlace place) {
		return new SchedulePlaceDeleteResBody(
			place.getId(),
			place.getContentId(),
			place.getPlaceType(),
			place.getDayOrder(),
			place.getOrderInDay());
	}
}
