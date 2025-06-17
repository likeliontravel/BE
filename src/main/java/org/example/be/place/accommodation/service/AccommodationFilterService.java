package org.example.be.place.accommodation.service;

import lombok.RequiredArgsConstructor;

import org.example.be.place.accommodation.dto.AccommodationResponseDTO;
import org.example.be.place.accommodation.entity.Accommodation;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.example.be.place.theme.PlaceCategoryRepository;
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
    private final TourRegionRepository tourRegionRepository;
    private final PlaceCategoryRepository placeCategoryRepository;

    // 숙소 필터링
    public List<AccommodationResponseDTO> getFilteredAccommodations(List<String> regions, List<String> themes, String keyword, Pageable pageable) {
        List<Accommodation> accommodations = accommodationRepository.findByFilters(regions, themes, keyword, pageable);

        if (accommodations.isEmpty()) {
            throw new NoSuchElementException("요청하신 조건에 해당하는 숙소가 존재하지 않습니다.");
        }

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

}

