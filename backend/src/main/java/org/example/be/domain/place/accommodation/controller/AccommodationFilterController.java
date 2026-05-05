package org.example.be.domain.place.accommodation.controller;

import org.example.be.domain.place.accommodation.dto.AccommodationResBody;
import org.example.be.domain.place.accommodation.service.AccommodationFilterService;
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
public class AccommodationFilterController {

	private final AccommodationFilterService accommodationFilterService;

	/**
	 * 숙소 필터링 API
	 * 쿼리 파라미터에
	 * accommodations
	 * regions
	 * themes
	 * keyword
	 * page
	 * size
	 * sortType(PlaceSortType)
	 * 입력 가능.
	 * accommodations, regions, themes는 nullable
	 * page, size, sort는 기본값 정해져있음(nullable)
	 */
	@GetMapping("/accommodations")
	public ResponseEntity<CommonResponse<PageResponse<AccommodationResBody>>> getFilteredAccommodations(
		@ModelAttribute PlaceSearchReqBody reqBody
	) {
		Pageable pageable = PageRequest.of(reqBody.page() - 1, reqBody.size(),
			Sort.by(reqBody.sortType().getSortDirection(), reqBody.sortType().getSortProperty()));

		PageResponse<AccommodationResBody> result = accommodationFilterService.getFilteredAccommodations(reqBody,
			pageable);
		return ResponseEntity.ok(CommonResponse.success(result, "숙소 필터링 조회 성공"));
	}

}
