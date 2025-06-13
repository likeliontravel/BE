package org.example.be.place.touristSpot.service;

import lombok.RequiredArgsConstructor;

import org.example.be.place.touristSpot.dto.TouristSpotDTO;
import org.example.be.place.touristSpot.entity.TouristSpot;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TouristSpotFilterService {

    private final TouristSpotRepository touristSpotRepository;

    public List<TouristSpotDTO> getFilteredTouristSpots(List<String> regions, List<String> themes, String keyword, Pageable pageable) {
        List<TouristSpot> filtered = touristSpotRepository.findAllByFilters(regions, themes, keyword, pageable);
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