package org.example.be.external.tourapi.controller;

import java.util.List;

import org.example.be.domain.place.accommodation.dto.AccommodationResBody;
import org.example.be.domain.place.restaurant.dto.RestaurantResBody;
import org.example.be.domain.place.touristspot.dto.TouristSpotResBody;
import org.example.be.external.tourapi.dto.FetchResult;
import org.example.be.external.tourapi.service.AccommodationFetchService;
import org.example.be.external.tourapi.service.RestaurantFetchService;
import org.example.be.external.tourapi.service.TouristSpotFetchService;
import org.example.be.external.tourapi.util.AreaCodeResolver;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.CommonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tourism")
@RequiredArgsConstructor
public class TourismController {

	private final TouristSpotFetchService touristSpotFetchService;
	private final AccommodationFetchService accommodationFetchService;
	private final RestaurantFetchService restaurantFetchService;
	private final AreaCodeResolver areaCodeResolver;

	private static final Logger logger = LoggerFactory.getLogger(TourismController.class);

	// 관광지 정보 저장
	@GetMapping("/fetch/touristSpot/{areaCode}")
	public ResponseEntity<CommonResponse<List<TouristSpotResBody>>> fetchTouristSpots(
		@PathVariable int areaCode,
		@RequestParam(defaultValue = "1") int pageNo
	) {
		String state = areaCodeResolver.getState(areaCode);
		if (state == null) {
			throw new BusinessException(ErrorCode.INVALID_REGION, "areaCode: " + areaCode);
		}

		List<TouristSpotResBody> result = touristSpotFetchService.getTouristSpots(
			areaCode, state, 12, 1000, pageNo
		);

		return ResponseEntity.ok(CommonResponse.success(result, "관광지 정보 저장 성공"));
	}

	// 숙소 정보(Accommodation)를 TourAPI에서 가져와 중복을 제거하고 저장하는 엔드포인트
	@GetMapping("/fetch/accommodation/{areaCode}")
	public ResponseEntity<CommonResponse<List<AccommodationResBody>>> fetchAccommodations(
		@PathVariable int areaCode, @RequestParam(defaultValue = "1") int pageNo
	) {
		String state = areaCodeResolver.getState(areaCode);
		if (state == null) {
			throw new BusinessException(ErrorCode.INVALID_REGION, "areaCode: " + areaCode);
		}
		List<AccommodationResBody> result = accommodationFetchService.getAccommodations(
			areaCode, state, 1000, pageNo
		);

		return ResponseEntity.ok(CommonResponse.success(result, "숙소 정보 저장 성공"));
	}

	// 식당 정보 저장
	@GetMapping("/fetch/restaurant/{areaCode}")
	public ResponseEntity<CommonResponse<List<RestaurantResBody>>> fetchRestaurants(
		@PathVariable int areaCode,
		@RequestParam(defaultValue = "1") int pageNo
	) {
		String state = areaCodeResolver.getState(areaCode);

		if (state == null) {
			throw new BusinessException(ErrorCode.INVALID_REGION, "areaCode: " + areaCode);
		}
		List<RestaurantResBody> result = restaurantFetchService.getData(areaCode, 39, 1000, pageNo);
		return ResponseEntity.ok(CommonResponse.success(result, "식당 정보 저장 성공"));
	}

	// 현재 관광지 정보 일괄 저장 / 업데이트
	@GetMapping("/fetch/touristSpot/all")
	public ResponseEntity<CommonResponse<FetchResult>> fetchAllTouristSpots(
		@RequestParam(defaultValue = "12") int contentTypeId,
		@RequestParam(defaultValue = "9999") int numOfRows
	) {
		FetchResult result = touristSpotFetchService.fetchAllTourData(contentTypeId, numOfRows);
		return ResponseEntity.ok(CommonResponse.success(result, "TouristSpot 전체 저장 성공"));
	}
}
