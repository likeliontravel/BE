package org.example.be.domain.schedule.dto.request;

// updateSchedulePlace()는 기존 schedulePlaceReqBody를 이용할 수 없음
// 기존 존재 블록에 대해서는 schedulePlaceId 필드 값을 받아야 함
// 신규 블록의 경우에는 schedulePlaceId를 받지 않고 신규 발급

import java.time.LocalDateTime;

import org.example.be.domain.place.shared.type.PlaceType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SchedulePlaceUpdateReqBody(

	// 기존 블록이면 이 컬럼 값 포함해 요청이 옴. 신규 블록이면 null.
	Long schedulePlaceId,

	@NotBlank(message = "장소 ID를 입력해주세요.")
	String contentId,

	@NotNull(message = "장소 타입을 선택해주세요.")
	PlaceType placeType,

	@NotNull(message = "방문 시작 시간을 입력해주세요.")
	LocalDateTime visitStart,

	@NotNull(message = "방문 종료 시간을 입력해주세요.")
	LocalDateTime visitedEnd,

	@NotNull(message = "방문 일차를 입력해주세요.")
	@Min(value = 1, message = "방문 일차는 1 이상이어야 합니다.")
	Integer dayOrder,

	@NotNull(message = "방문 순서를 입력해주세요.")
	@Min(value = 1, message = "방문 순서는 1 이상이어야 합니다.")
	Integer orderInDay

) {

}
