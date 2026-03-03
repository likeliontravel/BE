package org.example.be.group.dto;

import java.time.LocalDateTime;

import org.example.be.place.entity.PlaceType;

// 방문 장소 ( 장소 블록 세부정보 ) DTO
public record GroupSchedulePlaceDTO(
	String contentId,
	PlaceType placeType,    // 장소 타입 (관광지 / 숙소 / 식당)
	String title,
	String address,
	LocalDateTime visitStart,
	LocalDateTime visitEnd,
	int dayOrder,
	int orderInDay
) {
}
