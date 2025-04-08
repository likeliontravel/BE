package org.example.be.testing;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tourism")
@RequiredArgsConstructor
public class TourismController {

    private final TouristSpotService touristSpotService;
    @GetMapping("/fetch/{areaCode}")
    public ResponseEntity<List<TouristSpotDTO>> fetchTouristSpots(@PathVariable String areaCode) {
        try {
            String state = getStateByAreaCode(Integer.parseInt(areaCode));
            if (state == null) {
                return ResponseEntity.badRequest().build();
            }

            // 실제 데이터 받아오기
            List<TouristSpotDTO> touristSpots = touristSpotService.getTouristSpotDTOs(Integer.parseInt(areaCode), state, 12, 10);
            return ResponseEntity.ok(touristSpots);

        } catch (Exception e) {
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


//    private static final Logger logger = LoggerFactory.getLogger(TourismController.class);
//
//    // 동기적으로 관광지 데이터를 가져오기
//    @GetMapping("/fetch/{areaCode}")
//    public ResponseEntity<List<Map<String, Object>>> fetchTouristSpots(@PathVariable String areaCode) {
//        try {
//            // areaCode를 지역명으로 변환
//            String state = getStateByAreaCode(Integer.parseInt(areaCode));
//
//            if (state == null) {
//                return ResponseEntity.badRequest().body(null); // 잘못된 areaCode 처리
//            }
//
//            // 관광지 데이터를 가져옴
//            List<Map<String, Object>> touristSpots = touristSpotService.getData(Integer.parseInt(areaCode), state, 12, 10);
//            return ResponseEntity.ok(touristSpots);
//        } catch (Exception e) {
//            logger.error("관광지 정보를 가져오는 중 오류 발생: ", e);
//            return ResponseEntity.internalServerError().build(); // 에러 처리
//        }
//    }
//
//    private String getStateByAreaCode(int areaCode) {
//        Map<Integer, String> areaCodeMap = new HashMap<>();
//        areaCodeMap.put(1, "서울");
//        areaCodeMap.put(2, "인천");
//        areaCodeMap.put(3, "대전");
//        areaCodeMap.put(4, "대구");
//        areaCodeMap.put(5, "광주");
//        areaCodeMap.put(6, "부산");
//        areaCodeMap.put(7, "울산");
//        areaCodeMap.put(8, "세종");
//        areaCodeMap.put(31, "경기도");
//        areaCodeMap.put(32, "강원");
//        areaCodeMap.put(33, "충북");
//        areaCodeMap.put(34, "충남");
//        areaCodeMap.put(35, "경북");
//        areaCodeMap.put(36, "경남");
//        areaCodeMap.put(37, "전북");
//        areaCodeMap.put(38, "전라남도");
//        areaCodeMap.put(39, "제주도");
//
//        return areaCodeMap.get(areaCode);
//    }
/*    // 지역 코드로 관광지 목록 조회
    @GetMapping("/{areaCode}")
    public Mono<ResponseEntity<List<TouristSpot>>> getTouristSpots(@PathVariable String areaCode) {
        return Mono.just(ResponseEntity.ok(touristSpotService.getTouristSpotsByAreaCode(areaCode)))
                .doOnTerminate(() -> logger.info("지역 코드 {}에 대한 관광지 정보가 조회되었습니다.", areaCode));
    }*/
}
