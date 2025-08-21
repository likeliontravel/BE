package org.example.be.schedule.service;

import lombok.RequiredArgsConstructor;
import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.group.service.GroupService;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.example.be.place.entity.PlaceType;
import org.example.be.place.region.TourRegion;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.example.be.place.theme.PlaceCategory;
import org.example.be.place.theme.PlaceCategoryRepository;
import org.example.be.place.touristSpot.entity.TouristSpot;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.example.be.schedule.dto.*;
import org.example.be.schedule.entity.Schedule;
import org.example.be.schedule.entity.SchedulePlace;
import org.example.be.schedule.repository.SchedulePlaceRepository;
import org.example.be.schedule.repository.ScheduleRepository;
import org.example.be.security.util.SecurityUtil;
import org.example.be.unifieduser.entity.UnifiedUser;
import org.example.be.unifieduser.repository.UnifiedUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final GroupRepository groupRepository;
    private final PlaceValidationService placeValidationService;
    private final GroupService groupService;
    private final SchedulePlaceRepository schedulePlaceRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final AccommodationRepository accommodationRepository;
    private final RestaurantRepository restaurantRepository;
    private final TourRegionRepository tourRegionRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final UnifiedUserRepository unifiedUserRepository;

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

    // 일정 요약 목록 조회
    // 요청자의 가입된 그룹을 찾아 해당 그룹의 존재 일정 정보를 그룹별로 묶어 반환
    // 만약 그룹은 존재하나 일정이 없는 경우 scheduleFirstRegion값으로 "아직 일정이 생성되지 않았습니다" 전달 및 나머지값 null 반환
    @Transactional(readOnly = true)
    public List<ScheduleSummaryDTO> getScheduleList() {
        String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
        UnifiedUser user = unifiedUserRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new NoSuchElementException("해당 유저가 존재하지 않습니다."));

        List<Group> groups = groupRepository.findByMembersContaining(user);
        if (groups.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScheduleSummaryDTO> scheduleSummaryList = new ArrayList<>();

        for (Group group : groups) {
            Optional<Schedule> scheduleOpt = scheduleRepository.findByGroup(group);

            // 스케줄이 없으면 안내문구만 반환
            if (scheduleOpt.isEmpty()) {
                scheduleSummaryList.add(
                        ScheduleSummaryDTO.builder()
                                .groupName(group.getGroupName())
                                .scheduleFirstRegion("아직 일정이 생성되지 않았습니다.")
                                .scheduleFirstTheme(null)
                                .startSchedule(null)
                                .endSchedule(null)
                                .build()
                );
                continue;
            }

            Schedule schedule = scheduleOpt.get();
            List<SchedulePlace> places = schedulePlaceRepository.findBySchedule(schedule);

            // 1) region: 타입 무관, 가장 이른 visitStart
            SchedulePlace firstAnyPlace = places.stream()
                    .min(Comparator.comparing(SchedulePlace::getVisitStart))
                    .orElse(null);
            String firstRegionName = resolveRegion(firstAnyPlace); // TouristSpot/Restaurant/Accommodation 모두 처리

            // 2) theme: TouristSpot 중 가장 이른 visitStart가 있을 때만, 없으면 "기타"
            SchedulePlace firstTouristSpotPlace = places.stream()
                    .filter(p -> p.getPlaceType() == PlaceType.TouristSpot)
                    .min(Comparator.comparing(SchedulePlace::getVisitStart))
                    .orElse(null);
            String firstTheme = resolveThemeFromTouristSpot(firstTouristSpotPlace);

            scheduleSummaryList.add(
                    ScheduleSummaryDTO.builder()
                            .groupName(group.getGroupName())
                            .scheduleFirstRegion(firstRegionName)
                            .scheduleFirstTheme(firstTheme)
                            .startSchedule(schedule.getStartSchedule())
                            .endSchedule(schedule.getEndSchedule())
                            .build()
            );
        }

        return scheduleSummaryList;
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


    // ----------------------------- 내부 헬퍼 메서드 ---------------------------
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

    /**
     * placeType에 맞게 레포지토리에서 장소를 찾아 areaCode/siGunGuCode로 region을 구한다.
     * 장소를 찾지 못하면 null 반환.
     */
    private String resolveRegion(SchedulePlace place) {
        if (place == null) {
            return null;
        }

        switch (place.getPlaceType()) {
            case TouristSpot: {
                TouristSpot ts = touristSpotRepository.findByContentId(place.getContentId()).orElse(null);
                if (ts == null) return null;
                return tourRegionRepository
                        .findByAreaCodeAndSiGunGuCode(ts.getAreaCode(), ts.getSiGunGuCode())
                        .map(TourRegion::getRegion)
                        .orElse(null);
            }
            case Restaurant: {
                org.example.be.place.restaurant.entity.Restaurant r =
                        restaurantRepository.findByContentId(place.getContentId()).orElse(null);
                if (r == null) return null;
                return tourRegionRepository
                        .findByAreaCodeAndSiGunGuCode(r.getAreaCode(), r.getSiGunGuCode())
                        .map(TourRegion::getRegion)
                        .orElse(null);
            }
            case Accommodation: {
                org.example.be.place.accommodation.entity.Accommodation a =
                        accommodationRepository.findByContentId(place.getContentId()).orElse(null);
                if (a == null) return null;
                return tourRegionRepository
                        .findByAreaCodeAndSiGunGuCode(a.getAreaCode(), a.getSiGunGuCode())
                        .map(TourRegion::getRegion)
                        .orElse(null);
            }
            default:
                return null;
        }
    }

    /**
     * TouristSpot이 있을 때만 theme을 추출. 없으면 "기타".
     */
    private String resolveThemeFromTouristSpot(SchedulePlace touristSpotPlace) {
        if (touristSpotPlace == null) {
            return "기타";
        }
        TouristSpot ts = touristSpotRepository.findByContentId(touristSpotPlace.getContentId()).orElse(null);
        if (ts == null) {
            return "기타";
        }
        return placeCategoryRepository.findByCat3(ts.getCat3())
                .map(PlaceCategory::getTheme)
                .orElse("기타");
    }


}
