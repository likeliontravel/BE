package org.example.be.tour.service;


import lombok.RequiredArgsConstructor;

import org.example.be.tour.dto.AccommodationResponseDTO;
import org.example.be.tour.dto.RestaurantResponseDTO;
import org.example.be.tour.entity.Accommodation;
import org.example.be.tour.entity.Restaurant;
import org.example.be.tour.entity.TourRegion;
import org.example.be.tour.repository.AccommodationRepository;
import org.example.be.tour.repository.RestaurantRepository;
import org.example.be.tour.repository.TourRegionRepository;
import org.example.be.tour.theme.PlaceCategory;
import org.example.be.tour.theme.PlaceCategoryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceFilterService {

    private final AccommodationRepository accommodationRepository;
    private final TourRegionRepository tourRegionRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final RestaurantRepository restaurantRepository;

    // 숙소 필터링
    public List<AccommodationResponseDTO> getFilteredAccommodations(List<String> regions, List<String> themes, String keyword, Pageable pageable) {
        List<Accommodation> accommodations = accommodationRepository.findByFilters(regions, themes, keyword, pageable);
        return accommodations.stream()
                .map(this::toAccommodationResponseDTO)
                .collect(Collectors.toList());
    }

    // Accommodation -> ResponseDTO 변환
    private AccommodationResponseDTO toAccommodationResponseDTO(Accommodation accommodation) {
        String region = tourRegionRepository
                .findByAreaCodeAndSiGunGuCode(accommodation.getAreaCode(), accommodation.getSiGunGuCode())
                .map(TourRegion::getRegion)
                .orElse("기타");

        String theme = placeCategoryRepository
                .findByCat3(accommodation.getCat3())
                .map(PlaceCategory::getTheme)
                .orElse("기타");

        return AccommodationResponseDTO.builder()
                .contentId(accommodation.getContentId())
                .title(accommodation.getTitle())
                .imageUrl(accommodation.getThumbnailImageUrl())  // 반환할 사진은 300 X 200 정형화된 firstimage2
                .region(region)
                .theme(theme)
                .address(accommodation.getAddr1() + (accommodation.getAddr2() != null ? accommodation.getAddr2() : ""))
                .build();
    }

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
