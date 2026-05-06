package org.example.be.domain.place.restaurant.controller;

import org.example.be.domain.place.restaurant.dto.RestaurantResBody;
import org.example.be.domain.place.restaurant.service.RestaurantFilterService;
import org.example.be.domain.place.shared.dto.PlaceSearchReqBody;
import org.example.be.global.response.CommonResponse;
import org.example.be.global.response.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class RestaurantFilterController {

	private final RestaurantFilterService restaurantFilterService;

	// 식당 필터링 API
	@GetMapping("/restaurants")
	public ResponseEntity<CommonResponse<PageResponse<RestaurantResBody>>> getFilteredRestaurants(
		@ModelAttribute PlaceSearchReqBody reqBody
	) {
		Pageable pageable = PageRequest.of(
			reqBody.page() - 1, reqBody.size(),
			Sort.by(reqBody.sortType().getSortDirection(), reqBody.sortType().getSortProperty())
		);

		PageResponse<RestaurantResBody> result = restaurantFilterService.getFilteredRestaurants(reqBody, pageable);
		return ResponseEntity.ok(CommonResponse.success(result, "식당 필터링 조회 성공"));
	}

}