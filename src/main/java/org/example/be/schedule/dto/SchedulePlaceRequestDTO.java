package org.example.be.schedule.dto;

import lombok.*;
import org.example.be.schedule.entity.PlaceType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulePlaceRequestDTO {
    private Long scheduleId;
    private String contentId;
    private PlaceType placeType;
    private LocalDateTime visitStart;
    private LocalDateTime visitedEnd;
    private Integer dayOrder;
    private Integer orderInDay;
}
