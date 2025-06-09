package org.example.be.tour.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.be.tour.entity.Restaurant;
import org.example.be.tour.entity.TourRegion;
import org.example.be.tour.repository.RestaurantRepository;
import org.example.be.tour.repository.TourRegionRepository;
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
public class RestaurantService { //식당 정보 불러오기
    private final RestaurantRepository restaurantRepository;
    private final TourRegionRepository tourRegionRepository;

    @Value("${service-key}")
    private String serviceKey;

    public List<Map<String, Object>> getData(int areaCode, int sigunguCode, int contentTypeId, int numOfRows, int pageNo) throws Exception {
        return fetchAndSave(areaCode, sigunguCode, contentTypeId, numOfRows, pageNo);
    }
    
    
    public List<Map<String, Object>> getAllData(int areaCode, int sigunguCode, int contentTypeId, int numOfRows) throws Exception {
        int pageNo = 1;
        List<Map<String, Object>> allItems = new ArrayList<>();

        while (true) {
            List<Map<String, Object>> pageItems = fetchAndSave(areaCode, sigunguCode, contentTypeId, numOfRows, pageNo);
            if (pageItems.isEmpty()) break;

            allItems.addAll(pageItems);
            pageNo++;
        }

        return allItems;
    }
    // API 호출 및 DB 저장
    private List<Map<String, Object>> fetchAndSave(int areaCode, int sigunguCode, int contentTypeId, int numOfRows, int pageNo) throws Exception {
        String link = "https://apis.data.go.kr/B551011/KorService1/areaBasedList1";
        String decodedServiceKey = URLDecoder.decode(serviceKey, StandardCharsets.UTF_8);

        String url = UriComponentsBuilder.fromHttpUrl(link)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Test")
                .queryParam("_type", "json")
                .queryParam("areaCode", areaCode)
                .queryParam("sigunguCode", sigunguCode)
                .queryParam("contentTypeId", contentTypeId)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("serviceKey", serviceKey)
                .build(false)
                .toUriString();

        URI uri = new URI(url);
        RestTemplate restTemplate = new RestTemplate();

        String response = restTemplate.getForObject(uri, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(response, Map.class);

        Map<String, Object> responseMap = (Map<String, Object>) map.get("response");
        Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("body");
        Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");

        if (itemsMap == null || !itemsMap.containsKey("item")) {
            return new ArrayList<>();
        } // items가 없으면 가져올 데이터가 없다는 뜻

        List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemsMap.get("item");

        // DB 저장 및 중복 체크
        List<Map<String, Object>> filteredItems = itemList.stream()
                .peek(item -> {
                    Long contentId = Long.parseLong(item.get("contentid").toString());

                    boolean exists = restaurantRepository.existsByContentId(contentId);

                    if (!exists) {
                        Restaurant restaurant = new Restaurant();
                        restaurant.setContentId(contentId);
                        restaurant.setName(item.getOrDefault("title", "").toString());
                        restaurant.setAddress(item.getOrDefault("addr1", "").toString());
                        restaurant.setCategory(item.getOrDefault("cat3", "").toString());
                        restaurant.setImageUrl(item.getOrDefault("firstimage", "").toString());

                        // TourRegion 연동
                        Object areaCodeObj = item.get("areacode");
                        if (areaCodeObj != null) {
                            int areaCodeFromItem = Integer.parseInt(areaCodeObj.toString());
                            TourRegion region = tourRegionRepository.findFirstByAreaCode(areaCodeFromItem).orElse(null);
                            restaurant.setTourRegion(region);
                        }
                        restaurantRepository.save(restaurant);
                    }
                })
                .collect(Collectors.toList());

        return filteredItems;
    }
}