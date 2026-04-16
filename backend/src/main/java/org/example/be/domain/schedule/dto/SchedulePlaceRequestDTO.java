package org.example.be.domain.schedule.dto;

import java.time.LocalDateTime;

import org.example.be.domain.place.shared.type.PlaceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
