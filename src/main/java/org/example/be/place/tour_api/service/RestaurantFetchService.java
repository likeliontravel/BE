package org.example.be.place.tour_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be.place.dto.RestaurantDTO;
import org.example.be.place.entity.Restaurant;
import org.example.be.place.repository.RestaurantRepository;
import org.example.be.place.tour_api.util.TourApiClient;
import org.example.be.place.tour_api.util.TourApiParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantFetchService {

    private final TourApiClient tourApiClient;
    private final TourApiParser tourApiParser;
    private final RestaurantRepository restaurantRepository;

    @Value("${service-key}")
    private String serviceKey;

    //식당 데이터를 가져와 저장 및 DTO 리스트 반환하기
    public List<RestaurantDTO> getData(int areaCode, int contentTypeId, int numOfRows, int pageNo) throws Exception {
        String rawJson = tourApiClient.fetchTourData(areaCode, contentTypeId, numOfRows, pageNo, serviceKey); //tourApiClient에서 정보에 맞는 데이터를 가져옴

        List<Map<String, Object>> items = tourApiParser.parseItems(rawJson); //tourApiParser에서 데이터를 파싱함

        // DB에 중복 저장 & DTO 변환
        return items.stream()
                .peek(this::save)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // DB 저장 (contentId 중복 체크 후 신규만 저장)
    private void save(Map<String, Object> item) {
        String contentId = String.valueOf(item.get("contentid"));
        if (!restaurantRepository.existsByContentId(contentId)) {
            Restaurant restaurant = Restaurant.builder()
                    .contentId(contentId)
                    .title((String) item.get("title"))
                    .addr1((String) item.get("addr1"))
                    .addr2((String) item.get("addr2"))
                    .areaCode(String.valueOf(item.get("areacode")))
                    .siGunGuCode(String.valueOf(item.get("sigungucode")))
                    .cat1((String) item.get("cat1"))
                    .cat2((String) item.get("cat2"))
                    .cat3((String) item.get("cat3"))
                    .imageUrl((String) item.get("firstimage"))
                    .thumbnailImageUrl((String) item.get("firstimage2"))
                    .mapX(toDouble(item.get("mapx")))
                    .mapY(toDouble(item.get("mapy")))
                    .mLevel(toInteger(item.get("mlevel")))
                    .tel((String) item.get("tel"))
                    .modifiedTime((String) item.get("modifiedtime"))
                    .createdTime((String) item.get("createdtime"))
                    .build();

            restaurantRepository.save(restaurant);
        }
    }

    private RestaurantDTO toDTO(Map<String, Object> item) {
        return RestaurantDTO.builder()
                .contentId(String.valueOf(item.get("contentid")))
                .title((String) item.get("title"))
                .addr1((String) item.get("addr1"))
                .addr2((String) item.get("addr2"))
                .areaCode(String.valueOf(item.get("areacode")))
                .siGunGuCode(String.valueOf(item.get("sigungucode")))
                .cat1((String) item.get("cat1"))
                .cat2((String) item.get("cat2"))
                .cat3((String) item.get("cat3"))
                .imageUrl((String) item.get("firstimage"))
                .thumbnailImageUrl((String) item.get("firstimage2"))
                .mapX(toDouble(item.get("mapx")))
                .mapY(toDouble(item.get("mapy")))
                .mLevel(toInteger(item.get("mlevel")))
                .tel((String) item.get("tel"))
                .modifiedTime((String) item.get("modifiedtime"))
                .createdTime((String) item.get("createdtime"))
                .build();
    }

    private Double toDouble(Object obj) {
        try {
            return obj != null ? Double.parseDouble(obj.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Integer toInteger(Object obj) {
        try {
            return obj != null ? Integer.parseInt(obj.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
