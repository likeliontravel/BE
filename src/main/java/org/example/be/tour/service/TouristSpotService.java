package org.example.be.tour.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TouristSpotService {

    @Value("${service-key}")
    private String serviceKey;

    // 비동기 방식으로 관광지 데이터 가져오기
    public List<Map<String, Object>> getData(int areaCode, String state, int contentTypeId, int numOfRows) throws Exception {

        String link = "https://apis.data.go.kr/B551011/KorService1/areaBasedList1";
        String MobileOS = "ETC";
        String MobileApp = "Test";
        String _type = "json";

        // ✅ serviceKey 자동 디코딩 (안전한 방식)
        String decodedServiceKey = URLDecoder.decode(serviceKey, StandardCharsets.UTF_8);

        String url = UriComponentsBuilder.fromHttpUrl(link)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Test")
                .queryParam("_type", "json")
                .queryParam("areaCode", areaCode)
                .queryParam("contentTypeId", contentTypeId)
                .queryParam("numOfRows", numOfRows)
                .queryParam("serviceKey", serviceKey) // 인코딩 방지 적용
                .build(false)  // 자동 인코딩 방지!
                .toUriString();

        System.out.println("Generated URL: " + url); // 디버깅용 로그

        URI uri = new URI(url);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        String response = restTemplate.getForObject(uri, String.class);

        // JSON 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(response, Map.class);

        Map<String, Object> responseMap = (Map<String, Object>) map.get("response");
        Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("body");
        Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");
        List<Map<String, Object>> itemMap = (List<Map<String, Object>>) itemsMap.get("item");

        // state에 있는 정보만 필터링
        List<Map<String, Object>> filteredItems = itemMap.stream()
                .filter(item -> {
                    Object value = item.get("addr1");
                    return value != null && value.toString().contains(state);
                })
                .collect(Collectors.toList());

        return filteredItems;
    }
}
