package org.example.be.domain.place.accommodation.controller;

import java.util.List;

import org.example.be.domain.place.accommodation.dto.AccommodationDTO;
import org.example.be.domain.place.accommodation.service.AccommodationFilterService;
import org.example.be.domain.place.common.type.PlaceSortType;
import org.example.be.global.response.CommonResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Min;
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
	public ResponseEntity<CommonResponse<List<AccommodationDTO>>> getFilteredAccommodations(
		@RequestParam(required = false) List<String> regions,
		@RequestParam(required = false) List<String> themes,
		@RequestParam(required = false) String keyword,
		@RequestParam(defaultValue = "1") @Min(1) int page,
		@RequestParam(defaultValue = "30") @Min(1) int size,
		@RequestParam(defaultValue = "TITLE_ASC") PlaceSortType sortType
	) {
		Pageable pageable = PageRequest.of(page - 1, size,
			Sort.by(sortType.getSortDirection(), sortType.getSortProperty()));
		List<AccommodationDTO> result = accommodationFilterService.getFilteredAccommodations(regions, themes, keyword,
			pageable);
		return ResponseEntity.ok(CommonResponse.success(result, "숙소 필터링 조회 성공"));
	}

}
