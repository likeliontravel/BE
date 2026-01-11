package org.example.be.group.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// 그룹 일정 요약 + 장소 포함 응답용 DTO
@Getter
@Setter
public class GroupScheduleDTO {
    private LocalDateTime startSchedule;
    private LocalDateTime endSchedule;
    private List<GroupSchedulePlaceDTO> places;
}
