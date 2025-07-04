package org.example.be.place.restaurant.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.be.place.restaurant.service.RestaurantFilterService;
import org.example.be.place.restaurant.dto.RestaurantResponseDTO;
import org.example.be.place.entity.PlaceSortType;
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
public class RestaurantFilterController {

    private final RestaurantFilterService restaurantFilterService;

    // 식당 필터링 API
    @GetMapping("/restaurants")
    public ResponseEntity<CommonResponse<List<RestaurantResponseDTO>>> getFilteredRestaurants(
            @RequestParam(required = false) List<String> regions,
            @RequestParam(required = false) List<String> themes,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "30") @Min(1) int size,
            @RequestParam(defaultValue = "TITLE_ASC") PlaceSortType sortType
    ){
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortType.getSortDirection(), sortType.getSortProperty()));
        List<RestaurantResponseDTO> result = restaurantFilterService.getFilteredRestaurants(regions, themes, keyword, pageable);
        return ResponseEntity.ok(CommonResponse.success(result, "식당 필터링 조회 성공"));
    }

}