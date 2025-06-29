package org.example.be.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ScheduleResponseDTO {
    private Long scheduleId;
    private LocalDateTime startSchedule;
    private LocalDateTime endSchedule;
    private String groupName;
    private List<SchedulePlaceResponseDTO> schedulePlaces;
}
