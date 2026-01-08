package org.example.be.place.accommodation.service;

import lombok.RequiredArgsConstructor;

import org.example.be.exception.custom.BadParameterException;
import org.example.be.place.accommodation.dto.AccommodationDTO;
import org.example.be.place.accommodation.dto.AccommodationResponseDTO;
import org.example.be.place.accommodation.entity.Accommodation;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.example.be.place.region.TourRegionService;
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
public class AccommodationFilterService {

    private final AccommodationRepository accommodationRepository;
    private final TourRegionService tourRegionService;
    private final PlaceCategoryService placeCategoryService;

    // 숙소 필터링
    public List<AccommodationDTO> getFilteredAccommodations(List<String> regions, List<String> themes, String keyword, Pageable pageable) {

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

        List<Accommodation> accommodations = accommodationRepository.findAllByFilters(regions, themes, keyword, pageable);

        return accommodations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AccommodationDTO convertToDTO(Accommodation accommodation) {

        String region = (accommodation.getTourRegion() != null) ? accommodation.getTourRegion().getRegion() : "기타";
        String theme = (accommodation.getPlaceCategory() != null) ? accommodation.getPlaceCategory().getTheme() : "기타";

        return AccommodationDTO.builder()
                .contentId(accommodation.getContentId())
                .title(accommodation.getTitle())
                .addr1(accommodation.getAddr1())
                .addr2(accommodation.getAddr2())
                .areaCode(accommodation.getAreaCode())
                .siGunGuCode(accommodation.getSiGunGuCode())
                .cat1(accommodation.getCat1())
                .cat2(accommodation.getCat2())
                .cat3(accommodation.getCat3())
                .imageUrl(accommodation.getImageUrl())
                .thumbnailImageUrl(accommodation.getThumbnailImageUrl())
                .mapX(accommodation.getMapX())
                .mapY(accommodation.getMapY())
                .mLevel(accommodation.getMLevel())
                .tel(accommodation.getTel())
                .createdTime(accommodation.getCreatedTime())
                .modifiedTime(accommodation.getModifiedTime())
                .theme(theme)
                .region(region)
                .build();
    }
//
//    // Accommodation -> ResponseDTO 변환 ( db를 한번 더 찔러서 조회하길래 convertToDTO로 변경. 문제 있을 시 돌려놓을 예정)
//    private AccommodationResponseDTO toAccommodationResponseDTO(Accommodation accommodation) {
//        String region = tourRegionRepository
//                .findByAreaCodeAndSiGunGuCode(accommodation.getAreaCode(), accommodation.getSiGunGuCode())
//                .map(TourRegion::getRegion)
//                .orElse("기타");
//
//        String theme = placeCategoryRepository
//                .findByCat3(accommodation.getCat3())
//                .map(PlaceCategory::getTheme)
//                .orElse("기타");
//
//        return AccommodationResponseDTO.builder()
//                .contentId(accommodation.getContentId())
//                .title(accommodation.getTitle())
//                .imageUrl(accommodation.getThumbnailImageUrl())  // 반환할 사진은 300 X 200 정형화된 firstimage2
//                .region(region)
//                .theme(theme)
//                .address(accommodation.getAddr1() + (accommodation.getAddr2() != null ? accommodation.getAddr2() : ""))
//                .build();
//    }

}

