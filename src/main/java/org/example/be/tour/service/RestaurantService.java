package org.example.be.tour.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.be.tour.entity.Location;
import org.example.be.tour.entity.Restaurant;
import org.example.be.tour.repository.LocationRepository;
import org.example.be.tour.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final LocationRepository locationRepository;

    @Value("${service-key}")
    private String serviceKey;

    public List<Map<String, Object>> getData(int areaCode, int contentTypeId, int numOfRows) throws Exception {

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
                .queryParam("contentTypeId", 39)
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
        if (itemsMap == null || !itemsMap.containsKey("item")) {
            return new ArrayList<>();
        }

        return (List<Map<String, Object>>) itemsMap.get("item");
    }
    public void saveRestaurants(int areaCode) throws Exception {
        List<Map<String,Object>> restaurants = getData(areaCode, 39, Integer.MAX_VALUE);
        System.out.println("총 받은 식당 수: " + restaurants.size());

        for (Map<String, Object> item : restaurants) {
            Long contentId = Long.parseLong(item.get("contentid").toString());

            if(restaurantRepository.findByContentId(contentId).isEmpty()){
                Restaurant restaurant = new Restaurant();

                restaurant.setName(item.get("title").toString()); //식당 이름
                restaurant.setAddress(item.getOrDefault("addr1","").toString());
                restaurant.setCategory(item.getOrDefault("cat3", "").toString());
                restaurant.setImageUrl(item.getOrDefault("firstimage", "").toString());
                restaurant.setContentId(contentId);

                // ✅ 지역 설정
                Object areaCodeObj = item.get("areacode");
                if (areaCodeObj != null) {
                    int areaCodeFromItem = Integer.parseInt(areaCodeObj.toString());
                    Location location = locationRepository.findByAreaCode(areaCodeFromItem).orElse(null);
                    restaurant.setLocation(location);
                }
                restaurantRepository.save(restaurant);
            }
        }
    }
}
