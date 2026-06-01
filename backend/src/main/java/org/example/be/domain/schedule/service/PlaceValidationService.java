package org.example.be.domain.schedule.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
