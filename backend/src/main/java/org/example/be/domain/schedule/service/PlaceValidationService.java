package org.example.be.domain.schedule.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.be.domain.place.accommodation.entity.Accommodation;
import org.example.be.domain.place.accommodation.repository.AccommodationRepository;
import org.example.be.domain.place.restaurant.entity.Restaurant;
import org.example.be.domain.place.restaurant.repository.RestaurantRepository;
import org.example.be.domain.place.shared.type.PlaceType;
import org.example.be.domain.place.touristspot.entity.TouristSpot;
import org.example.be.domain.place.touristspot.repository.TouristSpotRepository;
import org.example.be.domain.schedule.dto.response.PlaceSimpleResBody;
import org.example.be.domain.schedule.entity.SchedulePlace;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceValidationService {

	private final TouristSpotRepository touristSpotRepository;
	private final RestaurantRepository restaurantRepository;
	private final AccommodationRepository accommodationRepository;

	public void validateContentIdByPlaceType(PlaceType placeType, String contentId) {
		boolean exists = switch (placeType) {
			case TOURISTSPOT -> touristSpotRepository.findByContentId(contentId).isPresent();
			case RESTAURANT -> restaurantRepository.findByContentId(contentId).isPresent();
			case ACCOMMODATION -> accommodationRepository.findByContentId(contentId).isPresent();
		};

		if (!exists) {
			throw new BusinessException(ErrorCode.PLACE_NOT_FOUND,
				"placeType: " + placeType + ", contentId: " + contentId);
		}
	}

	// Map 입력에 담긴 장소들이 실제로 DB에 존재하는지 한꺼번에 검증 (SchedulePlace 생성/수정이 공유)
	// Map<PlaceType, Set<String>> 구조 : 각 타입(관광지/식당/숙소) 별로 contentId를 set으로 묶어 같은 타입끼리 IN절 한번에 조회하기 위함
	public void validateContentIdsExist(Map<PlaceType, Set<String>> contentIdsByType) {
		contentIdsByType.forEach((placeType, requestedIds) -> {

			// 타입별로 모았을 때 id가 없는 타입은 바로 검증 종료
			if (requestedIds.isEmpty()) {
				return;
			}

			// DB에 실제로 존재하는 contentId만 추려오기
			Set<String> existingIds = findExistingContentIds(placeType, requestedIds);

			// 요청한 개수와 실제 개수가 다르면 그 차이만큼 없는 장소가 존재한다는 뜻.
			if (existingIds.size() != requestedIds.size()) {
				// 실제 없는 장소를 담을 Set
				Set<String> missingIds = new HashSet<>(requestedIds);
				missingIds.removeAll(existingIds); // 실제로 존재하는 contentId들만 요청받은 Id들에서 제거
				throw new BusinessException(ErrorCode.PLACE_NOT_FOUND,
					"placeType: " + placeType + ", 존재하지 않는 contentId: " + missingIds);
			}
		});
	}
	
	// 주어진 placeType의 contentId 묶음 중, 해당 테이블에 실제로 존재하는 contentId 집합만 반환
	// placeType에 따라 조회할 테이블이 달라지므로 validateContentIdByPlaceType()처럼 switch로 분기하며,
	// findAllByContentIdIn(IN절 사용)으로 묶음을 한 번에 조회해 N+1 해소
	private Set<String> findExistingContentIds(PlaceType placeType, Set<String> contentIds) {
		List<String> idList = new ArrayList<>(contentIds);
		return switch (placeType) {
			case TOURISTSPOT -> touristSpotRepository.findAllByContentIdIn(idList).stream()
				.map(TouristSpot::getContentId)
				.collect(Collectors.toSet());
			case RESTAURANT -> restaurantRepository.findAllByContentIdIn(idList).stream()
				.map(Restaurant::getContentId)
				.collect(Collectors.toSet());
			case ACCOMMODATION -> accommodationRepository.findAllByContentIdIn(idList).stream()
				.map(Accommodation::getContentId)
				.collect(Collectors.toSet());
		};
	}

	public Map<String, PlaceSimpleResBody> getPlaceSimpleDetails(List<SchedulePlace> schedulePlaces) {
		if (schedulePlaces == null || schedulePlaces.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<PlaceType, Set<String>> contentIdsByType = schedulePlaces.stream()
			.collect(Collectors.groupingBy(
				SchedulePlace::getPlaceType, Collectors.mapping(SchedulePlace::getContentId, Collectors.toSet())
			));

		Map<String, PlaceSimpleResBody> detailsMap = new HashMap<>();

		// 관광지
		Set<String> touristSpotIds = contentIdsByType.getOrDefault(PlaceType.TOURISTSPOT, Collections.emptySet());
		if (!touristSpotIds.isEmpty()) {
			detailsMap.putAll(touristSpotRepository.findAllByContentIdIn(new ArrayList<>(touristSpotIds))
				.stream()
				.collect(Collectors.toMap(TouristSpot::getContentId,
					t -> PlaceSimpleResBody.from(t.getTitle(), t.getThumbnailImageUrl(), t.getAddr1(), t.getAddr2()))));
		}

		// 음식점
		Set<String> restaurantIds = contentIdsByType.getOrDefault(PlaceType.RESTAURANT, Collections.emptySet());
		if (!restaurantIds.isEmpty()) {
			detailsMap.putAll(restaurantRepository.findAllByContentIdIn(new ArrayList<>(restaurantIds))
				.stream()
				.collect(Collectors.toMap(Restaurant::getContentId,
					t -> PlaceSimpleResBody.from(t.getTitle(), t.getThumbnailImageUrl(), t.getAddr1(), t.getAddr2()))));
		}

		// 숙박
		Set<String> accommodationIds = contentIdsByType.getOrDefault(PlaceType.ACCOMMODATION, Collections.emptySet());
		if (!accommodationIds.isEmpty()) {
			detailsMap.putAll(accommodationRepository.findAllByContentIdIn(new ArrayList<>(accommodationIds))
				.stream()
				.collect(Collectors.toMap(Accommodation::getContentId,
					t -> PlaceSimpleResBody.from(t.getTitle(), t.getThumbnailImageUrl(), t.getAddr1(), t.getAddr2()))));
		}

		return detailsMap;
	}
}
