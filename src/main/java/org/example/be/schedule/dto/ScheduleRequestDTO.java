package org.example.be.schedule.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ScheduleRequestDTO {

    private LocalDateTime startSchedule;

    private LocalDateTime endSchedule;

    private Long groupId;

    private List<SchedulePlaceRequestDTO> schedulePlaces;
}
