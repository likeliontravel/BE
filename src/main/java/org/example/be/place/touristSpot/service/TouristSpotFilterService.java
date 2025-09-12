package org.example.be.place.touristSpot.service;

import lombok.RequiredArgsConstructor;

import org.example.be.exception.custom.BadParameterException;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.place.region.TourRegionService;
import org.example.be.place.theme.PlaceCategoryService;
import org.example.be.place.touristSpot.dto.TouristSpotDTO;
import org.example.be.place.touristSpot.entity.TouristSpot;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TouristSpotFilterService {

    private final TouristSpotRepository touristSpotRepository;
    private final TourRegionService tourRegionService;
    private final PlaceCategoryService placeCategoryService;

    public List<TouristSpotDTO> getFilteredTouristSpots(List<String> regions, List<String> themes, String keyword, Pageable pageable) {

        if (regions != null) {
            for (String region : regions) {
                if (!tourRegionService.existsByRegion(region)) {
                    throw new BadParameterException("잘못된 지역(region)값이 포함되어 있습니다." + region);
                }
            }
        }

        if (themes != null) {
            for (String theme : themes) {
                if (!placeCategoryService.existsByTheme(theme)) {
                    throw new BadParameterException("잘못된 테마(theme)값이 포함되어 있습니다." + theme);
                }
            }
        }

        List<TouristSpot> filtered = touristSpotRepository.findAllByFilters(regions, themes, keyword, pageable);

        if (filtered.isEmpty()) {
            throw new NoSuchElementException("요청하진 조건에 해당하는 관광지가 존재하지 않습니다.");
        }

        return filtered.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TouristSpotDTO convertToDTO(TouristSpot spot) {
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
                .build();
    }
}