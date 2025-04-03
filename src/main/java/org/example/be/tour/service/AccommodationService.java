package org.example.be.tour.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.be.tour.entity.Accommodation;
import org.example.be.tour.entity.Location;
import org.example.be.tour.repository.AccommodationRepository;
import org.example.be.tour.repository.LocationRepository;
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
public class AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final LocationRepository locationRepository;

    @Value("${service-key}")
    private String serviceKey;

    public List<Map<String, Object>> getData(int areaCode, int numOfRows) throws Exception {

        String link = "https://apis.data.go.kr/B551011/KorService1/searchStay1";
        String MobileOS = "ETC";
        String MobileApp = "Test";
        String _type = "json";

        // ✅ serviceKey 자동 디코딩 (안전한 방식)
        String decodedServiceKey = URLDecoder.decode(serviceKey, StandardCharsets.UTF_8);

        String url = UriComponentsBuilder.fromHttpUrl(link)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Test")
                .queryParam("_type", "json")
                .queryParam("numOfRows", numOfRows)
                .queryParam("listYN", "Y")
                .queryParam("arrange", "A")
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

        List<Map<String, Object>> filteredItems = itemMap.stream()
                .filter(item -> {
                    Object areaCodeObj = item.get("areacode");
                    if (areaCodeObj == null) {
                        return false;
                    }
                    try {
                        int parsedAreaCode = Integer.parseInt(areaCodeObj.toString().trim());
                        return parsedAreaCode == areaCode;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        return filteredItems;
    }

    public void saveAccommodations(int areaCode) throws Exception {
        List<Map<String, Object>> accommodations = getData(areaCode, Integer.MAX_VALUE);

        for (Map<String, Object> item : accommodations) {
            Long contentId = Long.parseLong(item.get("contentid").toString());

            // 중복 검사 후 저장
            if (accommodationRepository.findByContentId(contentId).isEmpty()) {
                Accommodation accommodation = new Accommodation();

                accommodation.setName(item.get("title").toString()); // 숙소 이름
                accommodation.setAddress(item.getOrDefault("addr1", "").toString()); // 주소
                accommodation.setCategory(item.getOrDefault("cat3", "").toString()); // 카테고리 (예: B02010700)
                accommodation.setImageUrl(item.getOrDefault("firstimage", "").toString()); // 이미지
                accommodation.setContentId(contentId); // contentId 저장


                // 저장
                accommodationRepository.save(accommodation);
            }
        }
    }
}