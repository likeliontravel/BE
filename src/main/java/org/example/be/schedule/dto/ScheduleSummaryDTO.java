package org.example.be.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ScheduleSummaryDTO {
    private String groupName;               // 그룹 이름
    private String scheduleFirstRegion;     // 일정 이름(= 첫 방문 장소 지역명)
    private String scheduleFirstTheme;      // 첫 번째 TouristSpot의 테마 (없을 시 "기타")
    private LocalDateTime startSchedule;    // 일정 시작 시각 (D-Day활용)
    private LocalDateTime endSchedule;      // 일정 종료 시각 (D-Day활용)
}
