package org.example.be.domain.schedule.dto;

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

    private String groupName;

    private List<SchedulePlaceRequestDTO> schedulePlaces;
}
