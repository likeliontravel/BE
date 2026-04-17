package org.example.be.domain.schedule.dto.request;

import java.time.LocalDateTime;

import org.example.be.domain.place.shared.type.PlaceType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SchedulePlaceReqBody(
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
