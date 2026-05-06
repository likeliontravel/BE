package org.example.be.domain.place.touristspot.service;

import java.util.List;

import org.example.be.domain.place.region.TourRegionService;
import org.example.be.domain.place.shared.dto.PlaceSearchReqBody;
import org.example.be.domain.place.theme.PlaceCategoryService;
import org.example.be.domain.place.touristspot.dto.TouristSpotResBody;
import org.example.be.domain.place.touristspot.entity.TouristSpot;
import org.example.be.domain.place.touristspot.repository.TouristSpotRepository;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TouristSpotFilterService {

	private final TouristSpotRepository touristSpotRepository;
	private final TourRegionService tourRegionService;
	private final PlaceCategoryService placeCategoryService;

	public PageResponse<TouristSpotResBody> getFilteredTouristSpots(PlaceSearchReqBody reqBody, Pageable pageable) {

		List<String> regions = reqBody.regions();
		List<String> themes = reqBody.themes();
		String keyword = reqBody.keyword();

		// 파라미터가 빈 리스트라면 null 로 변환 → JPQL에서 무시되도록
		if (regions != null && regions.isEmpty()) {
			regions = null;
		}
		if (themes != null && themes.isEmpty()) {
			themes = null;
		}
		if (keyword != null && keyword.isBlank()) {
			keyword = null;
		}

		// region 파라미터 검증
		if (regions != null) {
			for (String region : regions) {
				if (!tourRegionService.existsByRegion(region)) {
					throw new BusinessException(ErrorCode.INVALID_REGION, "허용되지 않는 region값: " + region);
				}
			}
		}

		// theme 파라미터 검증
		if (themes != null) {
			for (String theme : themes) {
				if (!placeCategoryService.existsByTheme(theme)) {
					throw new BusinessException(ErrorCode.INVALID_THEME, "허용되지 않는 theme값: " + theme);
				}
			}
		}

		Page<TouristSpot> page = touristSpotRepository.findAllByFilters(regions, themes, keyword, pageable);

		List<TouristSpotResBody> content = page.getContent().stream()
			.map(TouristSpotResBody::from)
			.toList();

		return PageResponse.from(page, content);
	}
}
