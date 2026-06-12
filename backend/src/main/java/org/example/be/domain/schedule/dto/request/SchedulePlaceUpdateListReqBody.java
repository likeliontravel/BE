package org.example.be.domain.schedule.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

// PUT /schedule/detail/{scheduleId} (일정 블록 수정 API)의 요청 바디 래퍼
// List 를 객체 한 겹으로 감싸 (1) @Valid가 원소까지 cascade되게 하고, (2) @NotEmpty로 min-1을 건다.
// 빈 배열은 400으로 거부된다(옛 "빈 배열=전체삭제" 폐기 - 전체삭제는 전용 DELETE /detail/{scheduleId})
public record SchedulePlaceUpdateListReqBody(
	@NotEmpty(message = "일정을 수정하려면 최소 1개의 장소를 포함하여야 합니다.")    // min-1(null, 빈 배열 모두 거부)
	@Valid    // 원소들까지 validator가 cascade되도록 추가
	List<SchedulePlaceUpdateReqBody> schedulePlaces
) {
}
