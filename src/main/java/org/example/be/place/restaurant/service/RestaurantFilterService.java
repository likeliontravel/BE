package org.example.be.place.restaurant.service;

import lombok.RequiredArgsConstructor;

import org.example.be.exception.custom.BadParameterException;
import org.example.be.place.region.TourRegionService;
import org.example.be.place.restaurant.dto.RestaurantResponseDTO;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.place.restaurant.entity.Restaurant;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.example.be.place.theme.PlaceCategoryRepository;
import org.example.be.place.theme.PlaceCategoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.example.be.place.region.TourRegion;
import org.example.be.place.theme.PlaceCategory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantFilterService {

    private final TourRegionRepository tourRegionRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final TourRegionService tourRegionService;
    private final PlaceCategoryService placeCategoryService;

    // 식당 필터링
    public List<RestaurantResponseDTO> getFilteredRestaurants(List<String> regions, List<String> themes, String keyword, Pageable pageable) {
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
                    throw new BadParameterException("잘못된 지역(region)값이 포함되어 있습니다." + region);
                }
            }
        }

        // theme 파라미터 검증
        if (themes != null) {
            for (String theme : themes) {
                if (!placeCategoryService.existsByTheme(theme)) {
                    throw new BadParameterException("잘못된 테마(theme)값이 포함되어 있습니다." + theme);
                }
            }
        }

        List<Restaurant> restaurants = restaurantRepository.findByFilters(regions, themes, keyword, pageable);


        return restaurants.stream()
                .map(this::toRestaurantResponseDTO)
                .collect(Collectors.toList());
    }

    // Restaurant -> ResponseDTO 변환
    private RestaurantResponseDTO toRestaurantResponseDTO(Restaurant restaurant) {
        String region = tourRegionRepository
                .findByAreaCodeAndSiGunGuCode(restaurant.getAreaCode(), restaurant.getSiGunGuCode())
                .map(TourRegion::getRegion)
                .orElse("기타");

        String theme = placeCategoryRepository
                .findByCat3(restaurant.getCat3())
                .map(PlaceCategory::getTheme)
                .orElse("기타");

        return RestaurantResponseDTO.builder()
                .contentId(restaurant.getContentId())
                .title(restaurant.getTitle())
                .imageUrl(restaurant.getThumbnailImageUrl())  // 300 X 200 정형화된 firstimage2 이미지
                .region(region)
                .theme(theme)
                .address(restaurant.getAddr1() + (restaurant.getAddr2() != null ? restaurant.getAddr2() : ""))
                .build();
    }

}

