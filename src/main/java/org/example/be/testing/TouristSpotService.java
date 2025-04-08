package org.example.be.testing;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TouristSpotService {

    @Value("${service-key}")
    private String serviceKey;

    @Qualifier("tourApiWebClient")
    private final WebClient tourApiWebClient;

    public List<TouristSpotDTO> getTouristSpotDTOs(int areaCode, String state, int contentTypeId, int numOfRows) throws Exception {
        String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        String url = UriComponentsBuilder.fromPath("/areaBasedList1")
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Test")
                .queryParam("_type", "json")
                .queryParam("areaCode", areaCode)
                .queryParam("contentTypeId", contentTypeId)
                .queryParam("numOfRows", numOfRows)
                .queryParam("serviceKey", encodedServiceKey)
                .toUriString();

        String response = tourApiWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // ✅ 로그 찍어보기
        System.out.println("🔍 TourAPI 응답 결과:");
        System.out.println(response);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result = objectMapper.readValue(response, Map.class);

        Map<String, Object> responseMap = (Map<String, Object>) result.get("response");
        if (responseMap == null) return List.of();

        Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("body");
        if (bodyMap == null) return List.of();

        Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");
        if (itemsMap == null) return List.of();

        List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemsMap.get("item");
        if (itemList == null) return List.of();

        return itemList.stream()
                .filter(item -> {
                    Object addr = item.get("addr1");
                    return addr != null && addr.toString().contains(state);
                })
                .map(item -> TouristSpotDTO.builder()
                        .contentid((String) item.get("contentid"))
                        .title((String) item.get("title"))
                        .addr1((String) item.get("addr1"))
                        .areacode((String) item.get("areacode"))
                        .sigungucode((String) item.get("sigungucode"))
                        .cat1((String) item.get("cat1"))
                        .cat2((String) item.get("cat2"))
                        .cat3((String) item.get("cat3"))
                        .firstimage((String) item.get("firstimage"))
                        .firstimage2((String) item.get("firstimage2"))
                        .mapx(parseDouble(item.get("mapx")))
                        .mapy(parseDouble(item.get("mapy")))
                        .tel((String) item.get("tel"))
                        .modifiedtime((String) item.get("modifiedtime"))
                        .createdtime((String) item.get("createdtime"))
                        .build())
                .collect(Collectors.toList());
    }

    private Double parseDouble(Object value) {
        try {
            return value != null ? Double.parseDouble(value.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
