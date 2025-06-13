package org.example.be.place.restaurant.service;

import lombok.RequiredArgsConstructor;

import org.example.be.place.restaurant.dto.RestaurantResponseDTO;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.place.restaurant.entity.Restaurant;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.example.be.place.theme.PlaceCategoryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.example.be.place.region.TourRegion;
import org.example.be.place.theme.PlaceCategory;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantFilterService {

    private final TourRegionRepository tourRegionRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final RestaurantRepository restaurantRepository;

    // 식당 필터링
    public List<RestaurantResponseDTO> getFilteredRestaurants(List<String> regions, List<String> themes, String keyword, Pageable pageable) {
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

