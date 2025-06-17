package org.example.be.schedule.service;

import lombok.RequiredArgsConstructor;
import org.example.be.schedule.dto.SchedulePlaceRequestDTO;
import org.example.be.schedule.entity.PlaceType;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlaceValidationService {

    private final TouristSpotRepository touristSpotRepository;
    private final RestaurantRepository restaurantRepository;
    private final AccommodationRepository accommodationRepository;

    /**
     * 장소 타입과 contentId에 따라 존재하는지 확인
     */
    public void validateContentIdByPlaceType(PlaceType placeType, String contentId) {
        boolean exists = switch (placeType) {
            case TouristSpot -> touristSpotRepository.findByContentId(contentId).isPresent();
            case Restaurant -> restaurantRepository.findByContentId(contentId).isPresent();
            case Accommodation -> accommodationRepository.findByContentId(contentId).isPresent();
        };

        if (!exists) {
            throw new NoSuchElementException("존재하지 않는 장소입니다.");
        }
    }

    /**
     * 방문 시작 시간과 종료 시간 유효성 확인
     */
    public void validateVisitTime(LocalDateTime visitStart, LocalDateTime visitedEnd) {
        if (visitStart == null || visitedEnd == null || !visitStart.isBefore(visitedEnd)) {
            throw new IllegalArgumentException("방문 시작 시간은 종료 시간보다 이전이어야 합니다.");
        }
    }

    /**
     * SchedulePlace 목록 내에서 dayOrder, orderInDay 중복 검사 및 시간 유효성 검사
     */
    public void validateSchedulePlaceList(List<SchedulePlaceRequestDTO> places) {
        Map<Integer, Set<Integer>> dayOrderMap = new HashMap<>();

        for (SchedulePlaceRequestDTO place : places) {
            // 시간 유효성 체크
            validateVisitTime(place.getVisitStart(), place.getVisitedEnd());

            // dayOrder - orderInDay 중복 체크
            dayOrderMap.putIfAbsent(place.getDayOrder(), new HashSet<>());
            if (!dayOrderMap.get(place.getDayOrder()).add(place.getOrderInDay())) {
                throw new IllegalArgumentException(
                        String.format("%d일차의 %d번째 장소가 중복됩니다.", place.getDayOrder(), place.getOrderInDay())
                );
            }
        }
    }
}
