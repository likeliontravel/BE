package org.example.be.domain.schedule.service;

import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.domain.schedule.dto.SchedulePlaceRequestDTO;
import org.example.be.domain.schedule.dto.SchedulePlaceResponseDTO;
import org.example.be.domain.schedule.entity.SchedulePlace;
import org.example.be.domain.schedule.repository.SchedulePlaceRepository;
import org.example.be.domain.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulePlaceService {

	private final SchedulePlaceRepository schedulePlaceRepository;
	private final ScheduleRepository scheduleRepository;
	private final PlaceValidationService placeValidationService;

	@Transactional
	public SchedulePlaceResponseDTO createSchedulePlace(SchedulePlaceRequestDTO dto) {
		var schedule = scheduleRepository.findById(dto.getScheduleId())
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "scheduleId: " + dto.getScheduleId().toString()));

		placeValidationService.validateContentIdByPlaceType(dto.getPlaceType(), dto.getContentId());  // 변경

		SchedulePlace schedulePlace = SchedulePlace.builder()
			.schedule(schedule)
			.contentId(dto.getContentId())
			.placeType(dto.getPlaceType())
			.visitStart(dto.getVisitStart())
			.visitedEnd(dto.getVisitedEnd())
			.dayOrder(dto.getDayOrder())
			.orderInDay(dto.getOrderInDay())
			.build();

		try {
			var saved = schedulePlaceRepository.save(schedulePlace);
			return toResponseDTO(saved);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_CREATION_FAILED, "세부 일정 생성 실패 - message: " + e.getMessage());
		}
	}

	@Transactional
	public SchedulePlaceResponseDTO updateSchedulePlace(Long placeId, SchedulePlaceRequestDTO dto) {
		var place = schedulePlaceRepository.findById(placeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_PLACE_NOT_FOUND, "placeId: " + placeId));

		placeValidationService.validateContentIdByPlaceType(dto.getPlaceType(), dto.getContentId());  // 변경

		place.setContentId(dto.getContentId());
		place.setPlaceType(dto.getPlaceType());
		place.setVisitStart(dto.getVisitStart());
		place.setVisitedEnd(dto.getVisitedEnd());
		place.setDayOrder(dto.getDayOrder());
		place.setOrderInDay(dto.getOrderInDay());

		try {
			return toResponseDTO(schedulePlaceRepository.save(place));
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_UPDATE_FAILED, "세부 일정 수정 실패 - message: " + e.getMessage());
		}
	}

	@Transactional
	public void deleteSchedulePlace(Long placeId) {
		var place = schedulePlaceRepository.findById(placeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_PLACE_NOT_FOUND, "placeId: " + placeId));
		try {
			schedulePlaceRepository.delete(place);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_DELETE_FAILED, "세부 일정 삭제 실패 - message: " + e.getMessage());
		}
	}

	private SchedulePlaceResponseDTO toResponseDTO(SchedulePlace place) {
		return SchedulePlaceResponseDTO.builder()
			.id(place.getId())
			.contentId(place.getContentId())
			.placeType(place.getPlaceType())
			.visitStart(place.getVisitStart())
			.visitedEnd(place.getVisitedEnd())
			.dayOrder(place.getDayOrder())
			.orderInDay(place.getOrderInDay())
			.build();
	}
}
