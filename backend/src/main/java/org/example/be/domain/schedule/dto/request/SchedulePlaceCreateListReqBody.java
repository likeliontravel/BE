package org.example.be.domain.schedule.dto.request;

import java.util.List;

import jakarta.validation.Valid;

// POST /schedule/detail/{scheduleId} (일정 블록 생성 API)의 요청 바디 래퍼
// @Valid로 원소 검증을 살리는 것이 목적. '빈 배열' 정책은 기존 no-op 허용을 유지하기로 확정
// -> 여기에는 @NotEmpty 미부착: 빈 배열은 거부하지 말고 통과시킨다 (수정과 달리 생성에는 블록이 비어도 상관 없음)
public record SchedulePlaceCreateListReqBody(
	@Valid
	List<SchedulePlaceReqBody> schedulePlaces
) {
}
