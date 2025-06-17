package org.example.be.schedule.service;

import lombok.RequiredArgsConstructor;
import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.schedule.dto.SchedulePlaceRequestDTO;
import org.example.be.schedule.dto.ScheduleRequestDTO;
import org.example.be.schedule.dto.ScheduleResponseDTO;
import org.example.be.schedule.entity.PlaceType;
import org.example.be.schedule.entity.Schedule;
import org.example.be.schedule.entity.SchedulePlace;
import org.example.be.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final GroupRepository groupRepository;
    private final PlaceValidationService placeValidationService;  // 새로 주입

    @Transactional
    public ScheduleResponseDTO createSchedule(ScheduleRequestDTO requestDTO) {
        Group group = groupRepository.findById(requestDTO.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("해당 그룹을 찾을 수 없습니다."));

        placeValidationService.validateSchedulePlaceList(requestDTO.getSchedulePlaces());

        Schedule schedule = Schedule.builder()
                .startSchedule(requestDTO.getStartSchedule())
                .endSchedule(requestDTO.getEndSchedule())
                .group(group)
                .build();

        for (SchedulePlaceRequestDTO places : requestDTO.getSchedulePlaces()) {
            placeValidationService.validateContentIdByPlaceType(places.getPlaceType(), places.getContentId());

            SchedulePlace schedulePlace = SchedulePlace.builder()
                    .schedule(schedule)
                    .contentId(places.getContentId())
                    .placeType(places.getPlaceType())
                    .visitStart(places.getVisitStart())
                    .visitedEnd(places.getVisitedEnd())
                    .dayOrder(places.getDayOrder())
                    .orderInDay(places.getOrderInDay())
                    .build();

            schedule.getSchedulePlaces().add(schedulePlace);
        }

        try {
            Schedule savedSchedule = scheduleRepository.save(schedule);
            return new ScheduleResponseDTO(savedSchedule.getId());
        } catch (Exception e) {
            throw new ResourceCreationException("일정 생성에 실패했습니다.", e);
        }
    }

    @Transactional
    public ScheduleResponseDTO updateSchedule(Long scheduleId, ScheduleRequestDTO requestDTO) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 일정입니다."));

        Group group = groupRepository.findById(requestDTO.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        schedule.setStartSchedule(requestDTO.getStartSchedule());
        schedule.setEndSchedule(requestDTO.getEndSchedule());
        schedule.setGroup(group);

        schedule.getSchedulePlaces().clear();

        for (SchedulePlaceRequestDTO places : requestDTO.getSchedulePlaces()) {
            placeValidationService.validateContentIdByPlaceType(places.getPlaceType(), places.getContentId());  // 변경됨

            SchedulePlace schedulePlace = SchedulePlace.builder()
                    .schedule(schedule)
                    .contentId(places.getContentId())
                    .placeType(places.getPlaceType())
                    .visitStart(places.getVisitStart())
                    .visitedEnd(places.getVisitedEnd())
                    .dayOrder(places.getDayOrder())
                    .orderInDay(places.getOrderInDay())
                    .build();

            schedule.getSchedulePlaces().add(schedulePlace);
        }
        try {
            Schedule updatedSchedule = scheduleRepository.save(schedule);
            return new ScheduleResponseDTO(updatedSchedule.getId());
        } catch (Exception e) {
            throw new ResourceUpdateException("일정 수정에 실패했습니다.", e);
        }
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 일정입니다."));

        try {
            scheduleRepository.delete(schedule);
        } catch (Exception e) {
            throw new ResourceDeletionException("일정 삭제에 실패했습니다.", e);
        }
    }


}
