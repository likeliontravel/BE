package org.example.be.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be.place.dto.TouristSpotDTO;
import org.example.be.place.entity.TouristSpot;
import org.example.be.place.repository.TouristSpotRepository;
import org.example.be.place.util.TourApiClient;
import org.example.be.place.util.TourApiParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TouristSpotService {

    private final TouristSpotRepository touristSpotRepository;
    private final TourApiClient tourApiClient;
    private final TourApiParser tourApiParser;

    @Value("${service-key}")
    private String serviceKey;

    public List<TouristSpotDTO> getTouristSpots(int areaCode, String state, int contentTypeId, int numOfRows, int pageNo) throws Exception {
        if (pageNo <= 0) {
            return getAllData(areaCode, state, contentTypeId, numOfRows);
        } else {
            return getPageData(areaCode, state, contentTypeId, numOfRows, pageNo);
        }
    }

    // 페이지 입력 시 fetch
    private List<TouristSpotDTO> getPageData(int areaCode, String state, int contentTypeId, int numOfRows, int pageNo) throws Exception {
        String json = tourApiClient.fetchTourData(areaCode, contentTypeId, numOfRows, pageNo, serviceKey);
        log.debug("[TourAPI JSON 응답] {}", json);
        List<Map<String, Object>> items = tourApiParser.parseItems(json);
        log.debug("[Debug] parsed items size: " + items.size());

        return items.stream()
                .filter(item -> item.get("addr1") != null && item.get("addr1").toString().contains(state))
                .peek(this::saveIfNotExist)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 페이지 미입력시 전체 데이터 fetch
    private List<TouristSpotDTO> getAllData(int areaCode, String state, int contentTypeId, int numOfRows) throws Exception {
        int pageNo = 1;
        List<TouristSpotDTO> allItems = new ArrayList<>();

        while (true) {
            String json =tourApiClient.fetchTourData(areaCode, contentTypeId, numOfRows, pageNo, serviceKey);
            System.out.println("[Debug] Raw Json from tour api: \n" + json);
            log.debug("[Debug] Raw Json from tour api: \n {}", json);
            List<Map<String, Object>> items = tourApiParser.parseItems(json);
            log.debug("[Debug] parsed items size: {} ", items.size());

            List<Map<String, Object>> filtered = items.stream()
                    .filter(item -> {
                        log.debug("addr1: {}", item.get("addr1"));
                        return item.get("addr1") != null && item.get("addr1").toString().contains(state);
                    })
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) break;

            filtered.forEach(this::saveIfNotExist);
            allItems.addAll(filtered.stream().map(this::toDTO).toList());
            pageNo++;
        }

        return allItems;
    }

    // contentId로 조회 시 없는 경우 ( 기존에 없던 장소일 경우 ) 새로 저장
    private void saveIfNotExist(Map<String, Object> item) {
        String contentId = String.valueOf(item.get("contentid"));
        if (!touristSpotRepository.existsByContentId(contentId)) {
            TouristSpot touristSpot = TouristSpot.builder()
                    .contentId(contentId)
                    .title((String) item.get("title"))
                    .addr1((String) item.get("addr1"))
                    .addr2((String) item.get("addr2"))
                    .areaCode((String) item.get("areacode"))
                    .siGunGuCode((String) item.get("sigungucode"))
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
            touristSpotRepository.save(touristSpot);
        }
    }

    // TouristSpot -> DTO 변환
    private TouristSpotDTO toDTO(Map<String, Object> item) {
        return TouristSpotDTO.builder()
                .contentId((String) item.get("contentid"))
                .title((String) item.get("title"))
                .addr1((String) item.get("addr1"))
                .addr2((String) item.get("addr2"))
                .areaCode((String) item.get("areacode"))
                .siGunGuCode((String) item.get("sigungucode"))
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
        try{
            return obj != null ? Integer.parseInt(obj.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}