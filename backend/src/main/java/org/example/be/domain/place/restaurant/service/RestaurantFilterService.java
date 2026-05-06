package org.example.be.domain.place.restaurant.service;

import java.util.List;

import org.example.be.domain.place.region.TourRegionService;
import org.example.be.domain.place.restaurant.dto.RestaurantResBody;
import org.example.be.domain.place.restaurant.entity.Restaurant;
import org.example.be.domain.place.restaurant.repository.RestaurantRepository;
import org.example.be.domain.place.shared.dto.PlaceSearchReqBody;
import org.example.be.domain.place.theme.PlaceCategoryService;
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
public class RestaurantFilterService {
	
	private final RestaurantRepository restaurantRepository;
	private final TourRegionService tourRegionService;
	private final PlaceCategoryService placeCategoryService;

	// 식당 필터링
	public PageResponse<RestaurantResBody> getFilteredRestaurants(PlaceSearchReqBody reqBody, Pageable pageable) {

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

		Page<Restaurant> page = restaurantRepository.findAllByFilters(regions, themes, keyword, pageable);

		List<RestaurantResBody> content = page.getContent().stream()
			.map(RestaurantResBody::from)
			.toList();

		return PageResponse.from(page, content);
	}

	//    // Restaurant -> ResponseDTO 변환
	//    private RestaurantResponseDTO toRestaurantResponseDTO(Restaurant restaurant) {
	//        String region = tourRegionRepository
	//                .findByAreaCodeAndSiGunGuCode(restaurant.getAreaCode(), restaurant.getSiGunGuCode())
	//                .map(TourRegion::getRegion)
	//                .orElse("기타");
	//
	//        String theme = placeCategoryRepository
	//                .findByCat3(restaurant.getCat3())
	//                .map(PlaceCategory::getTheme)
	//                .orElse("기타");
	//
	//        return RestaurantResponseDTO.builder()
	//                .contentId(restaurant.getContentId())
	//                .title(restaurant.getTitle())
	//                .imageUrl(restaurant.getThumbnailImageUrl())  // 300 X 200 정형화된 firstimage2 이미지
	//                .region(region)
	//                .theme(theme)
	//                .address(restaurant.getAddr1() + (restaurant.getAddr2() != null ? restaurant.getAddr2() : ""))
	//                .build();
	//    }

}

