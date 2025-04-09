package org.example.test.controller;

import lombok.RequiredArgsConstructor;
import org.example.test.entity.TouristSpot;
import org.example.test.service.KeywordSearchService;
import org.example.test.service.TouristSpotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tourism")
@RequiredArgsConstructor
public class TourismController {

    private final TouristSpotService touristSpotService;
    private static final Logger logger = LoggerFactory.getLogger(TourismController.class);
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
}