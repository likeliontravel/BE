package org.example.test.controller;

import lombok.RequiredArgsConstructor;
import org.example.test.entity.TouristSpot;
import org.example.test.service.TouristSpotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/tourism")
@RequiredArgsConstructor
public class TourismController {

    private final TouristSpotService touristSpotService;
    private static final Logger logger = LoggerFactory.getLogger(TourismController.class);

    // 비동기적으로 관광지 데이터를 가져오기 및 저장
    @GetMapping("/fetch/{areaCode}")
    public Mono<ResponseEntity<List<TouristSpot>>> fetchAndSaveTouristSpots(@PathVariable String areaCode) {
        return touristSpotService.fetchAndSaveTouristSpots(areaCode)
                .doOnTerminate(() -> logger.info("관광지 정보가 성공적으로 저장되었습니다."))
                .map(savedSpots -> ResponseEntity.ok(savedSpots))
                .onErrorResume(e -> {
                    logger.error("관광지 정보를 가져오는 중 오류 발생: ", e);
                    return Mono.just(ResponseEntity.internalServerError().build()); // 에러 처리
                });
    }

    // 지역 코드로 관광지 목록 조회
    @GetMapping("/{areaCode}")
    public Mono<ResponseEntity<List<TouristSpot>>> getTouristSpots(@PathVariable String areaCode) {
        return Mono.just(ResponseEntity.ok(touristSpotService.getTouristSpotsByAreaCode(areaCode)))
                .doOnTerminate(() -> logger.info("지역 코드 {}에 대한 관광지 정보가 조회되었습니다.", areaCode));
    }
}