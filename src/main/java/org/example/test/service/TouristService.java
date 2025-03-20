package org.example.test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.test.dto.TouristAttractionDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class TouristService {

    private final WebClient webClient;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOURIST_KEY_PREFIX = "tourist:";

    public Mono<List<TouristAttractionDto>> getTouristAttractions(String city) {
        String redisKey = TOURIST_KEY_PREFIX + city;

        // Redis에서 캐시 조회
        String cachedData = redisTemplate.opsForValue().get(redisKey);
        if (cachedData != null) {
            log.info("Redis에서 캐시 데이터 반환: {}", city);
            return Mono.just(parseJsonToDtoList(cachedData));
        }

        // WebClient로 API 호출
        log.info(" 캐시 없음! WebClient로 API 호출: {}", city);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/tourapi/rest/searchKeyword1")
                        .queryParam("serviceKey", "YOUR_API_KEY")
                        .queryParam("keyword", city)
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseJsonToDtoList)
                .doOnSuccess(response -> {
                    // Redis에 데이터 저장 (TTL 1시간 설정)
                    redisTemplate.opsForValue().set(redisKey, convertDtoListToJson(response), Duration.ofHours(1));
                    log.info("Redis에 데이터 저장 완료: {}", city);
                });
    }

    private List<TouristAttractionDto> parseJsonToDtoList(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("response").path("body").path("items").path("item");
            return StreamSupport.stream(items.spliterator(), false)
                    .map(this::convertJsonNodeToDto)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private TouristAttractionDto convertJsonNodeToDto(JsonNode node) {
        return TouristAttractionDto.builder()
                .contentId(node.path("contentid").asText())
                .title(node.path("title").asText())
                .address(node.path("addr1").asText())
                .areaCode(node.path("areacode").asText())
                .sigunguCode(node.path("sigungucode").asText())
                .category1(node.path("cat1").asText())
                .category2(node.path("cat2").asText())
                .category3(node.path("cat3").asText())
                .imageUrl(node.path("firstimage").asText())
                .thumbnailUrl(node.path("firstimage2").asText())
                .mapX(node.path("mapx").asText())
                .mapY(node.path("mapy").asText())
                .phone(node.path("tel").asText())
                .modifiedTime(node.path("modifiedtime").asText())
                .createdTime(node.path("createdtime").asText())
                .build();
    }

    private String convertDtoListToJson(List<TouristAttractionDto> dtos) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(dtos);
        } catch (JsonProcessingException e) {
            log.error("DTO 리스트를 JSON으로 변환하는 중 오류 발생: {}", e.getMessage());
            return "[]";
        }
    }
}