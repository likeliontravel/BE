package org.example.be.place.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.place.dto.AccommodationResponseDTO;
import org.example.be.place.entity.PlaceSortType;
import org.example.be.place.service.PlaceFilterService;
import org.example.be.response.CommonResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceFilterController {

    private final PlaceFilterService placeFilterService;

    /**
     * 숙소 필터링 API
     * 쿼리 파라미터에
     * accommodations
     * regions
     * themes
     * keyword
     * page
     * size
     * sortType(PlaceSortType)
     * 입력 가능.
     * accommodations, regions, themes는 nullable
     * page, size, sort는 기본값 정해져있음(nullable)
     */
    @GetMapping("/accommodations")
    public ResponseEntity<CommonResponse<List<AccommodationResponseDTO>>> getFilteredAccommodations(
            @RequestParam(required = false) List<String> regions,
            @RequestParam(required = false) List<String> themes,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "TITLE_ASC")PlaceSortType sortType
            ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortType.getSortDirection(), sortType.getSortProperty()));
        List<AccommodationResponseDTO> result = placeFilterService.getFilteredAccommodations(regions, themes, keyword, pageable);
        return ResponseEntity.ok(CommonResponse.success(result, "숙소 필터링 조회 성공"));
    }
}
