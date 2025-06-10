package org.example.be.tour.controller;

import lombok.RequiredArgsConstructor;

import org.example.be.tour.dto.AccommodationDTO;
import org.example.be.tour.dto.RestaurantDTO;
import org.example.be.tour.dto.TouristSpotDTO;
import org.example.be.tour.service.AccommodationService;
import org.example.be.tour.service.RestaurantService;
import org.example.be.tour.service.TouristSpotService;
import org.example.be.tour.util.AreaCodeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tourism")
@RequiredArgsConstructor
public class TourismController {

    private final TouristSpotService touristSpotService;
    private final AccommodationService accommodationService;
    private final RestaurantService restaurantService;
    private final AreaCodeResolver areaCodeResolver;

    private static final Logger logger = LoggerFactory.getLogger(TourismController.class);

    // 관광지 정보 조회
    @GetMapping("/fetch/touristSpot/{areaCode}")
    public ResponseEntity<List<TouristSpotDTO>> fetchTouristSpots(
            @PathVariable String areaCode,
            @RequestParam(defaultValue = "1") int pageNo
    ) throws Exception {
        int code = Integer.parseInt(areaCode);
        String state = areaCodeResolver.getState(code);
        if (state == null) {
            throw new IllegalArgumentException("유효하지 않은 지역 코드입니다. areaCode: " + areaCode);
        }

        List<TouristSpotDTO> result = touristSpotService.getTouristSpots(
                code, state, 12, 1000, pageNo
        );

        return ResponseEntity.ok(result);
    }

    // 숙소 정보(Accommodation)를 TourAPI에서 가져와 중복을 제거하고 저장하는 엔드포인트
    @GetMapping("/fetch/accommodation/{areaCode}")
    public ResponseEntity<List<AccommodationDTO>> fetchAccommodations(
            @PathVariable String areaCode, @RequestParam(defaultValue = "1") int pageNo
    ) throws Exception {
        int code = Integer.parseInt(areaCode);
        String state = areaCodeResolver.getState(code);
        if (state == null) {
            throw new IllegalArgumentException("유효하지 않은 지역 코드입니다. areaCode: " + areaCode);
        }
        List<AccommodationDTO> result = accommodationService.getAccommodations(
                code, state, 1000, pageNo
        );

        return ResponseEntity.ok(result);
    }

    // 식당 정보 저장
    @GetMapping("/fetch/restaurant/{areaCode}")
    public ResponseEntity<List<RestaurantDTO>> fetchRestaurants(
            @PathVariable String areaCode,
            @RequestParam(defaultValue = "1") int pageNo
    ) throws Exception {
        int code = Integer.parseInt(areaCode);
        String state = areaCodeResolver.getState(code);

        if (state == null) {
            throw new IllegalArgumentException("유효하지 않은 지역 코드입니다. areaCode: " + areaCode);
        }
        List<RestaurantDTO> result = restaurantService.getData(code, 39, 1000, pageNo);
        return ResponseEntity.ok(result);
    }
}

