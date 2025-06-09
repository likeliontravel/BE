package org.example.be.tour.controller;

import lombok.RequiredArgsConstructor;

import org.example.be.tour.service.AccommodationService;
import org.example.be.tour.service.KeywordSearchService;
import org.example.be.tour.service.RestaurantService;
import org.example.be.tour.service.TouristSpotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tourism")
@RequiredArgsConstructor
public class TourismController {

    private final TouristSpotService touristSpotService;
    private static final Logger logger = LoggerFactory.getLogger(TourismController.class);
    private final AccommodationService accommodationService;
    private final RestaurantService restaurantService;
    private final KeywordSearchService keywordSearchService;

    // 동기적으로 관광지 데이터를 가져오기
    @GetMapping("/fetch/{areaCode}")
    public ResponseEntity<List<Map<String, Object>>> fetchTouristSpots(
            @PathVariable String areaCode,
            @RequestParam(defaultValue = "1") int pageNo //기본값 1
    ) {
        try {
            String state = getStateByAreaCode(Integer.parseInt(areaCode));
            if (state == null) return ResponseEntity.badRequest().build();

            List<Map<String, Object>> touristSpots;

            if (pageNo <= 0) {
                // 전체 페이지 반복 호출
                touristSpots = touristSpotService.getAllData(
                        Integer.parseInt(areaCode), state, 12
                );
            } else {
                // 지정한 페이지만 호출
                touristSpots = touristSpotService.getData(
                        Integer.parseInt(areaCode), state, 12, 1000, pageNo
                );
            }

            return ResponseEntity.ok(touristSpots);
        } catch (Exception e) {
            logger.error("관광지 정보를 가져오는 중 오류 발생: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "38") int contentTypeId
    ) {
        try {
            List<Map<String, Object>> result = keywordSearchService.searchByKeyword(keyword, contentTypeId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("키워드 검색 중 오류 발생: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    private String getStateByAreaCode(int areaCode) {
        Map<Integer, String> areaCodeMap = new HashMap<>();
        areaCodeMap.put(1, "서울");
        areaCodeMap.put(2, "인천");
        areaCodeMap.put(3, "대전");
        areaCodeMap.put(4, "대구");
        areaCodeMap.put(5, "광주");
        areaCodeMap.put(6, "부산");
        areaCodeMap.put(7, "울산");
        areaCodeMap.put(8, "세종");
        areaCodeMap.put(31, "경기도");
        areaCodeMap.put(32, "강원");
        areaCodeMap.put(33, "충북");
        areaCodeMap.put(34, "충남");
        areaCodeMap.put(35, "경북");
        areaCodeMap.put(36, "경남");
        areaCodeMap.put(37, "전북");
        areaCodeMap.put(38, "전라남도");
        areaCodeMap.put(39, "제주도");

        return areaCodeMap.get(areaCode);
    }

    @GetMapping("/accommodation/{areaCode}")
    public ResponseEntity<List<Map<String, Object>>> getAccommodations(@PathVariable int areaCode) {
        try {
            // API에서 100개 가져온 후, 지역 코드 필터링하여 10개만 반환
            List<Map<String, Object>> accommodations = accommodationService.getData(areaCode, Integer.MAX_VALUE)
                    .stream()
                    .limit(10) // 상위 10개만 선택
                    .collect(Collectors.toList());

            return ResponseEntity.ok(accommodations);
        } catch (Exception e) {
            logger.error("숙박 정보를 가져오는 중 오류 발생: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    @PostMapping("/accommodation/save/{areaCode}")
    public ResponseEntity<String> saveAccommodations(@PathVariable int areaCode) {
        try {
            accommodationService.saveAccommodations(areaCode);
            return ResponseEntity.ok("숙박 정보가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("숙박 정보 저장 중 오류 발생: " + e.getMessage());
        }
    }
    @GetMapping("/fetch/restaurants/{areaCode}/{sigunguCode}")
    public ResponseEntity<List<Map<String, Object>>> fetchRestaurants(
            @PathVariable int areaCode,
            @PathVariable int sigunguCode,
            @RequestParam(defaultValue = "1") int pageNo
    ) {
        try {
            int contentTypeId = 39;  // 음식점입니당.
            int numOfRows = 1000;

            List<Map<String, Object>> restaurants;

            if (pageNo <= 0) {
                restaurants = restaurantService.getAllData(areaCode, sigunguCode, contentTypeId, numOfRows);
            } else {
                // 지정한 페이지만 호출
                restaurants = restaurantService.getData(areaCode, sigunguCode, contentTypeId, numOfRows, pageNo);
            }

            return ResponseEntity.ok(restaurants);
        } catch (Exception e) {
            logger.error("식당 정보를 가져오는 중 오류 발생: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    }
//    @PostMapping("/restaurant/save/{areaCode}")
//    public ResponseEntity<String> saveRestaurants(@PathVariable int areaCode) {
//        try {
//            restaurantService.saveRestaurants(areaCode);
//            return ResponseEntity.ok("식당 정보가 성공적으로 저장되었습니다.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("식당 정보 저장 중 오류 발생: " + e.getMessage());
//        }
//    }


/*    // 지역 코드로 관광지 목록 조회
    @GetMapping("/{areaCode}")
    public Mono<ResponseEntity<List<TouristSpot>>> getTouristSpots(@PathVariable String areaCode) {
        return Mono.just(ResponseEntity.ok(touristSpotService.getTouristSpotsByAreaCode(areaCode)))
                .doOnTerminate(() -> logger.info("지역 코드 {}에 대한 관광지 정보가 조회되었습니다.", areaCode));
    }*/

