package org.example.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.test.dto.TouristSpotDTO;
import org.example.test.entity.TouristSpot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
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

    // 비동기 방식으로 관광지 데이터 가져오기
    public List<Map<String, Object>> getData(int areaCode, String state, int contentTypeId, int numOfRows) throws Exception {

        String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        String link = "https://apis.data.go.kr/B551011/KorService1/areaBasedList1";
        String MobileOS = "ETC";
        String MobileApp = "Test";
        String _type = "json";

        String url = UriComponentsBuilder.fromHttpUrl(link)
                .queryParam("MobileOS", MobileOS)
                .queryParam("MobileApp", MobileApp)
                .queryParam("_type", _type)
                .queryParam("areaCode", areaCode)
                .queryParam("contentTypeId", contentTypeId)
                .queryParam("numOfRows", numOfRows)
                .queryParam("serviceKey", encodedServiceKey) // 인코딩된 serviceKey 사용
                .toUriString();

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