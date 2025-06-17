package org.example.be.schedule.service;

import lombok.RequiredArgsConstructor;
import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.schedule.dto.SchedulePlaceRequestDTO;
import org.example.be.schedule.dto.SchedulePlaceResponseDTO;
import org.example.be.schedule.entity.SchedulePlace;
import org.example.be.schedule.repository.SchedulePlaceRepository;
import org.example.be.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SchedulePlaceService {

    private final SchedulePlaceRepository schedulePlaceRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlaceValidationService placeValidationService;

    @Transactional
    public SchedulePlaceResponseDTO createSchedulePlace(SchedulePlaceRequestDTO dto) {
        var schedule = scheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new NoSuchElementException("해당 일정이 존재하지 않습니다."));

        placeValidationService.validateContentIdByPlaceType(dto.getPlaceType(), dto.getContentId());
        placeValidationService.validateVisitTime(dto.getVisitStart(), dto.getVisitedEnd());

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
            throw new ResourceCreationException("세부 일정 생성에 실패했습니다.", e);
        }
    }

    @Transactional
    public SchedulePlaceResponseDTO updateSchedulePlace(Long placeId, SchedulePlaceRequestDTO dto) {
        var place = schedulePlaceRepository.findById(placeId)
                .orElseThrow(() -> new NoSuchElementException("해당 세부 일정이 존재하지 않습니다."));

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
            throw new ResourceUpdateException("세부 일정 수정에 실패했습니다.", e);
        }
    }

    @Transactional
    public void deleteSchedulePlace(Long placeId) {
        var place = schedulePlaceRepository.findById(placeId)
                .orElseThrow(() -> new NoSuchElementException("해당 세부 일정이 존재하지 않습니다."));
        try {
            schedulePlaceRepository.delete(place);
        } catch (Exception e) {
            throw new ResourceDeletionException("세부 일정 삭제에 실패했습니다.", e);
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
