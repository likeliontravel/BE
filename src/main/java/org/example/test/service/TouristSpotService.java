package org.example.test.service;

import lombok.RequiredArgsConstructor;
import org.example.test.entity.TouristSpot;
import org.example.test.repository.TouristSpotRepository;
import org.example.test.dto.TouristSpotDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TouristSpotService {

    @Value("${service-key}")
    private String serviceKey;

    @Value("${api.url}")
    private String apiUrl;  // api.url을 프로퍼티에서 주입받음

    private final TouristSpotRepository touristSpotRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TouristSpot> fetchAndSaveTouristSpots(String areaCode) {
        String url = apiUrl + "?serviceKey=" + serviceKey +
                "&MobileApp=AppTest&MobileOS=ETC&listYN=Y&arrange=A" +
                "&areaCode=" + areaCode + "&_type=json&numOfRows=10&pageNo=1";

        Mono<String> responseMono = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class);

        return responseMono.map(this::parseApiResponse)
                .doOnNext(touristSpotRepository::saveAll)
                .block();
    }

    private List<TouristSpot> parseApiResponse(String jsonResponse) {
        List<TouristSpot> touristSpots = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode items = root.path("response").path("body").path("items").path("item");

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return touristSpots;
    }

    // 추가된 메서드: areaCode로 관광지 목록을 조회
    public List<TouristSpot> getTouristSpotsByAreaCode(String areaCode) {
        return touristSpotRepository.findByAreaCode(areaCode);
    }
}