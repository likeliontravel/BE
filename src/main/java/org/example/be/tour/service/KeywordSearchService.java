//
//package org.example.be.tour.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.example.be.tour.entity.TouristSpot;
//import org.example.be.tour.repository.TouristSpotRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.net.URI;
//import java.net.URLDecoder;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class KeywordSearchService {
//
//    private final TouristSpotRepository touristSpotRepository;
//
//    @Value("${service-key}")
//    private String serviceKey;
//
//    public List<Map<String, Object>> searchByKeyword(String keyword, int contentTypeId) throws Exception {
//        String decodedServiceKey = URLDecoder.decode(serviceKey, StandardCharsets.UTF_8);
//        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
//
//        String url = UriComponentsBuilder.fromHttpUrl("https://apis.data.go.kr/B551011/KorService1/searchKeyword1")
//                .queryParam("MobileOS", "ETC")
//                .queryParam("MobileApp", "Test")
//                .queryParam("_type", "json")
//                .queryParam("keyword", encodedKeyword)
//                .queryParam("contentTypeId", contentTypeId)
//                .queryParam("numOfRows", 100)
//                .queryParam("pageNo", 1)
//                .queryParam("arrange", "C")
//                .queryParam("listYN", "Y")
//                .queryParam("serviceKey", serviceKey)
//                .build(false)
//                .toUriString();
//
//        URI uri = new URI(url);
//        RestTemplate restTemplate = new RestTemplate();
//        String response = restTemplate.getForObject(uri, String.class);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> map = objectMapper.readValue(response, Map.class);
//
//        Map<String, Object> responseMap = (Map<String, Object>) map.get("response");
//        Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("body");
//        Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");
//
//        if (itemsMap == null || !(itemsMap.get("item") instanceof List)) {
//            return List.of(); // 결과 없음
//        }
//
//        List<Map<String, Object>> itemMap = (List<Map<String, Object>>) itemsMap.get("item");
//
//        List<Map<String, Object>> filteredItems = itemMap.stream()
//                .peek(item -> {
//                    String contentId = String.valueOf(item.get("contentid"));
//                    boolean exists = touristSpotRepository.existsByContentId(contentId);
//
//                    if (!exists) {
//                        TouristSpot newSpot = TouristSpot.builder()
//                                .contentId(contentId)
//                                .title((String) item.get("title"))
//                                .address((String) item.get("addr1"))
//                                .areaCode(String.valueOf(item.get("areacode")))
//                                .sigunguCode(String.valueOf(item.get("sigungucode")))
//                                .category1((String) item.get("cat1"))
//                                .category2((String) item.get("cat2"))
//                                .category3((String) item.get("cat3"))
//                                .imageUrl((String) item.get("firstimage"))
//                                .thumbnailUrl((String) item.get("firstimage2"))
//                                .mapX(toDouble(item.get("mapx")))
//                                .mapY(toDouble(item.get("mapy")))
//                                .phone((String) item.get("tel"))
//                                .modifiedTime((String) item.get("modifiedtime"))
//                                .createdTime((String) item.get("createdtime"))
//                                .build();
//
//                        touristSpotRepository.save(newSpot);
//                    }
//                })
//                .collect(Collectors.toList());
//
//        return filteredItems;
//    }
//
//    private Double toDouble(Object obj) {
//        try {
//            return obj != null ? Double.parseDouble(obj.toString()) : null;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//}
//
