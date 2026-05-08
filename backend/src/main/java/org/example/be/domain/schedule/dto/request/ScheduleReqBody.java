package org.example.be.domain.schedule.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScheduleReqBody(
	@NotNull(message = "시작 일정을 입력해주세요.")
	LocalDateTime startSchedule,

	@NotNull(message = "종료 일정을 입력해주세요.")
	LocalDateTime endSchedule,

	@NotBlank(message = "그룹 이름을 입력해주세요.")
	String groupName
) {
}
