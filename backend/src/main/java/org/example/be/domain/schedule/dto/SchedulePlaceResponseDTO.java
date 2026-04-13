package org.example.be.domain.schedule.dto;

import lombok.*;

import org.example.be.domain.place.entity.PlaceType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulePlaceResponseDTO {

	private Long id;
	private String contentId;
	private PlaceType placeType;
	private LocalDateTime visitStart;
	private LocalDateTime visitedEnd;
	private Integer dayOrder;
	private Integer orderInDay;
}
