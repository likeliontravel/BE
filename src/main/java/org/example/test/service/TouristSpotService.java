package org.example.test.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.test.dto.TouristSpotDTO;
import org.example.test.entity.TouristSpot;
import org.example.test.repository.TouristSpotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TouristSpotService {

    @Value("${service-key}")
    private String serviceKey;

/*    @Value("${api.url}")
    private String apiUrl;*/

    private final TouristSpotRepository touristSpotRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 비동기 방식으로 관광지 데이터 가져오기 및 저장
    public Mono<List<TouristSpot>> fetchAndSaveTouristSpots(String areaCode) {
        String url = "http://apis.data.go.kr/B551011/KorService1/areaBasedList1" + "?serviceKey=" + serviceKey +
                "&MobileApp=AppTest&MobileOS=ETC&listYN=Y&arrange=A" +
                "&areaCode=" + areaCode + "&_type=xml&numOfRows=10&pageNo=1";

        // 요청 URL 확인
        System.out.println("Fetching URL: " + url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnTerminate(() -> System.out.println("API call completed")) // API 호출이 종료되었을 때
                .doOnError(e -> System.err.println("Error during API call: " + e.getMessage())) // 오류 발생 시
                .flatMapMany(this::parseApiResponse)
                .collectList()
                .flatMap(spots -> {
                    // List<TouristSpot> -> Mono<List<TouristSpot>> 형태로 저장
                    return Mono.just(touristSpotRepository.saveAll(spots))
                            .doOnTerminate(() -> System.out.println("Data saved to DB"))
                            .flatMap(savedSpots -> Mono.just(savedSpots)); // 저장된 데이터를 다시 Mono로 래핑
                });
    }

    private Flux<TouristSpot> parseApiResponse(String jsonResponse) {
        try {
            System.out.println("Parsing response: " + jsonResponse); // 응답 확인
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            List<TouristSpot> touristSpots = new ArrayList<>();
            for (JsonNode item : items) {
                TouristSpotDTO dto = objectMapper.treeToValue(item, TouristSpotDTO.class);
                TouristSpot spot = TouristSpot.builder()
                        .contentId(dto.getContentid())
                        .title(dto.getTitle())
                        .address(dto.getAddr1())
                        .areaCode(dto.getAreacode())
                        .sigunguCode(dto.getSigungucode())
                        .category1(dto.getCat1())
                        .category2(dto.getCat2())
                        .category3(dto.getCat3())
                        .imageUrl(dto.getFirstimage())
                        .thumbnailUrl(dto.getFirstimage2())
                        .mapX(dto.getMapx())
                        .mapY(dto.getMapy())
                        .phone(dto.getTel())
                        .modifiedTime(dto.getModifiedtime())
                        .createdTime(dto.getCreatedtime())
                        .build();
                touristSpots.add(spot);
            }
            return Flux.fromIterable(touristSpots);
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage()); // JSON 파싱 오류 확인
            return Flux.error(new RuntimeException("JSON 파싱 오류", e));
        }
    }
    // 특정 지역 코드로 관광지 목록 조회
    public List<TouristSpot> getTouristSpotsByAreaCode(String areaCode) {
        return touristSpotRepository.findByAreaCode(areaCode);
    }
}