package org.example.be.schedule.service;

import lombok.RequiredArgsConstructor;
import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.group.service.GroupService;
import org.example.be.schedule.dto.SchedulePlaceRequestDTO;
import org.example.be.schedule.dto.SchedulePlaceResponseDTO;
import org.example.be.schedule.dto.ScheduleRequestDTO;
import org.example.be.schedule.dto.ScheduleResponseDTO;
import org.example.be.schedule.entity.Schedule;
import org.example.be.schedule.entity.SchedulePlace;
import org.example.be.schedule.repository.ScheduleRepository;
import org.example.be.security.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final GroupRepository groupRepository;
    private final PlaceValidationService placeValidationService;  // 새로 주입
    private final GroupService groupService;

    // 일정 생성
    @Transactional
    public ScheduleResponseDTO createSchedule(ScheduleRequestDTO requestDTO) {
        Group group = groupRepository.findByGroupName(requestDTO.getGroupName())
                .orElseThrow(() -> new NoSuchElementException("해당 그룹을 찾을 수 없습니다."));

        // 그룹 창설자인지 검증
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        groupService.validateGroupCreator(group.getGroupName(), userIdentifier);

        // 이미 일정이 존재하는지 검사
        scheduleRepository.findByGroup(group).ifPresent(existingSchedule -> {
            throw new IllegalStateException("해당 그룹에 이미 일정이 존재합니다.");
        });

        Schedule schedule = Schedule.builder()
                .startSchedule(requestDTO.getStartSchedule())
                .endSchedule(requestDTO.getEndSchedule())
                .group(group)
                .build();

        for (SchedulePlaceRequestDTO places : requestDTO.getSchedulePlaces()) {
            placeValidationService.validateContentIdByPlaceType(places.getPlaceType(), places.getContentId()); // 변경됨

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
            return ScheduleResponseDTO.builder()
                    .scheduleId(savedSchedule.getId())
                    .startSchedule(savedSchedule.getStartSchedule())
                    .endSchedule(savedSchedule.getEndSchedule())
                    .schedulePlaces(
                            savedSchedule.getSchedulePlaces().stream()
                                    .map(this::toResponseDTO)
                                    .toList()
                    )
                    .build();
        } catch (Exception e) {
            throw new ResourceCreationException("일정 생성에 실패했습니다.", e);
        }
    }

    // 일정 조회
    @Transactional(readOnly = true)
    public ScheduleResponseDTO getScheduleByGroupName(String groupName) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("해당 이름의 그룹이 존재하지 않습니다."));

        Schedule schedule = scheduleRepository.findByGroup(group)
                .orElseThrow(() -> new NoSuchElementException("해당 그룹에 일정이 존재하지 않습니다."));

        List<SchedulePlaceResponseDTO> placeDTOs = schedule.getSchedulePlaces().stream()
                .map(this::toResponseDTO)
                .toList();

        return ScheduleResponseDTO.builder()
                .scheduleId(schedule.getId())
                .startSchedule(schedule.getStartSchedule())
                .endSchedule(schedule.getEndSchedule())
                .groupName(schedule.getGroup().getGroupName())
                .schedulePlaces(placeDTOs)
                .build();
    }

    // 일정 수정
    @Transactional
    public ScheduleResponseDTO updateSchedule(Long scheduleId, ScheduleRequestDTO requestDTO) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 일정입니다."));

        Group group = groupRepository.findByGroupName(requestDTO.getGroupName())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        // 그룹 창설자 검증
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        groupService.validateGroupCreator(group.getGroupName(), userIdentifier);

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
            return ScheduleResponseDTO.builder()
                    .scheduleId(updatedSchedule.getId())
                    .startSchedule(updatedSchedule.getStartSchedule())
                    .endSchedule(updatedSchedule.getEndSchedule())
                    .schedulePlaces(
                            updatedSchedule.getSchedulePlaces().stream()
                                    .map(this::toResponseDTO)
                                    .toList()
                    )
                    .build();
        } catch (Exception e) {
            throw new ResourceUpdateException("일정 수정에 실패했습니다.", e);
        }
    }

    // 일정 삭제
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 일정입니다."));

        // 그룹 창설자 검증
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        groupService.validateGroupCreator(schedule.getGroup().getGroupName(), userIdentifier);

        try {
            scheduleRepository.delete(schedule);
        } catch (Exception e) {
            throw new ResourceDeletionException("일정 삭제에 실패했습니다.", e);
        }
    }

    // 조회에 사용할 응답DTO 매퍼 ( SchedulePlace )
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
