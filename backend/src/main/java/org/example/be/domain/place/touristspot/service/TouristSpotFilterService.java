package org.example.be.domain.place.touristspot.service;

import java.util.List;
import java.util.stream.Collectors;

import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.domain.place.region.TourRegionService;
import org.example.be.domain.place.theme.PlaceCategoryService;
import org.example.be.domain.place.touristspot.dto.TouristSpotDTO;
import org.example.be.domain.place.touristspot.entity.TouristSpot;
import org.example.be.domain.place.touristspot.repository.TouristSpotRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TouristSpotFilterService {

	private final TouristSpotRepository touristSpotRepository;
	private final TourRegionService tourRegionService;
	private final PlaceCategoryService placeCategoryService;

	public List<TouristSpotDTO> getFilteredTouristSpots(List<String> regions, List<String> themes, String keyword,
		Pageable pageable) {

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

		List<TouristSpot> filtered = touristSpotRepository.findAllByFilters(regions, themes, keyword, pageable);

		return filtered.stream()
			.map(this::convertToDTO)
			.collect(Collectors.toList());
	}

	private TouristSpotDTO convertToDTO(TouristSpot spot) {

		String region = (spot.getTourRegion() != null) ? spot.getTourRegion().getRegion() : "기타";
		String theme = (spot.getPlaceCategory() != null) ? spot.getPlaceCategory().getTheme() : "기타";

		return TouristSpotDTO.builder()
			.contentId(spot.getContentId())
			.title(spot.getTitle())
			.addr1(spot.getAddr1())
			.addr2(spot.getAddr2())
			.areaCode(spot.getAreaCode())
			.siGunGuCode(spot.getSiGunGuCode())
			.cat1(spot.getCat1())
			.cat2(spot.getCat2())
			.cat3(spot.getCat3())
			.imageUrl(spot.getImageUrl())
			.thumbnailImageUrl(spot.getThumbnailImageUrl())
			.mapX(spot.getMapX())
			.mapY(spot.getMapY())
			.mLevel(spot.getMLevel())
			.tel(spot.getTel())
			.createdTime(spot.getCreatedTime())
			.modifiedTime(spot.getModifiedTime())
			.theme(theme)
			.region(region)
			.build();
	}
}