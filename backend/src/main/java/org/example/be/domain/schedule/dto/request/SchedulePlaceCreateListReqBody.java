package org.example.be.domain.schedule.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

// POST /schedule/detail/{scheduleId} (일정 블록 생성 API)의 요청 바디 래퍼
// List를 객체 한 겹으로 감싸 @Valid가 원소까지 cascade되게 하고, @NotEmpty로 min-1을 건다.
// 빈 배열은 400으로 거부된다(생성 시 최소 1개 블록 필수)
public record SchedulePlaceCreateListReqBody(
	@NotEmpty(message = "일정을 생성하려면 최소 1개의 장소를 포함하여야 합니다.")    // min-1(null, 빈 배열 모두 거부)
	@Valid
	List<SchedulePlaceReqBody> schedulePlaces
) {
}
