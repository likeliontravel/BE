package org.example.be.schedule.dto.response;

import java.time.LocalDateTime;

public record ScheduleSummaryResBody(
	String groupName, // 그룹 이름
	String scheduleFirstRegion, // 일정 이름(= 첫 방문 장소 지역명)
	String scheduleFirstTheme, // 첫 번째 TouristSpot의 테마 (없을 시 "기타")
	LocalDateTime startSchedule, // 일정 시작 시각 (D-Day활용)
	LocalDateTime endSchedule // 일정 종료 시각 (D-Day활용)
) {
}
