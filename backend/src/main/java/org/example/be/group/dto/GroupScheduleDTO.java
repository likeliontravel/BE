package org.example.be.group.dto;

import java.time.LocalDateTime;
import java.util.List;

// 그룹 일정 요약 + 장소 포함 DTO
public record GroupScheduleDTO(
	LocalDateTime startSchedule,
	LocalDateTime endSchedule,
	List<GroupSchedulePlaceDTO> places
) {
}
