package org.example.be.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be.place.dto.AccommodationDTO;
import org.example.be.place.entity.Accommodation;
import org.example.be.place.repository.AccommodationRepository;
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
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final TourApiClient tourApiClient;
    private final TourApiParser tourApiParser;

    @Value("${service-key}")
    private String serviceKey;

    // 컨트롤러 입력 -> 페이지 입력에 따라 데이터 저장 함수 분기
    public List<AccommodationDTO> getAccommodations(int areaCode, String state, int numOfRows, int pageNo) throws Exception {
        int contentTypeId = 32; // 숙소 ContentTypeId 32 고정
        if (pageNo <= 0) {
            return getAllData(areaCode, state, contentTypeId, numOfRows);
        } else {
            return getPageData(areaCode, state, contentTypeId, numOfRows, pageNo);
        }
    }

    // Page 입력 시 해당 단위 데이터 저장
    private List<AccommodationDTO> getPageData(int areaCode, String state, int contentTypeId, int numOfRows, int pageNo) throws Exception {
        String json = tourApiClient.fetchTourData(areaCode, contentTypeId, numOfRows, pageNo, serviceKey);
        log.debug("[TourAPI JSON 응답 - 숙소] \n {}",json);
        List<Map<String, Object>> items = tourApiParser.parseItems(json);
        log.debug("숙소 parse items size: {}", items.size());

        return items.stream()
                .filter(item -> item.get("addr1") != null && item.get("addr1").toString().contains(state))
                .peek(this::saveIfNotExist)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Page 미입력 시 전체 결과 저장
    private List<AccommodationDTO> getAllData(int areaCode, String state, int contentTypeId, int numOfRows) throws Exception {
        int pageNo = 1;
        List<AccommodationDTO> allItems = new ArrayList<>();

        while (true) {
            String json = tourApiClient.fetchTourData(areaCode, contentTypeId, numOfRows, pageNo, serviceKey);
            log.debug("[숙소 Raw Json] {}", json);
            List<Map<String, Object>> items = tourApiParser.parseItems(json);
            log.debug("[숙소 parsed items size: {}", items.size());

            List<Map<String, Object>> filtered = items.stream()
                    .filter(item -> item.get("addr1") != null && item.get("addr1").toString().contains(state))
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) break;

            filtered.forEach(this::saveIfNotExist);
            allItems.addAll(filtered.stream().map(this::toDTO).toList());
            pageNo++;
        }
        return allItems;
    }

    // DB에 저장 ( contentId가 존재하지 않는 것만 저장 )
    private void saveIfNotExist(Map<String, Object> item) {
        String contentId = String.valueOf(item.get("contentid"));
        if (!accommodationRepository.existsByContentId(contentId)) {
            Accommodation accommodation = Accommodation.builder()
                    .contentId(contentId)
                    .title(item.get("title").toString())
                    .addr1(item.get("addr1").toString())
                    .addr2(item.get("addr2").toString())
                    .areaCode((String) item.get("areacode"))
                    .cat1(item.get("cat1").toString())
                    .cat2(item.get("cat2").toString())
                    .cat3(item.get("cat3").toString())
                    .imageUrl((String) item.get("firstimage"))
                    .thumbnailImageUrl((String) item.get("firstimage2"))
                    .mapX(toDouble(item.get("mapx")))
                    .mapY(toDouble(item.get("mapy")))
                    .mLevel(toInteger(item.get("mlevel")))
                    .tel(item.get("tel").toString())
                    .modifiedTime((String) item.get("modifiedtime"))
                    .createdTime((String) item.get("createdtime"))
                    .build();
            accommodationRepository.save(accommodation);
        }
    }

    // Map<String, Object> -> DTO
    private AccommodationDTO toDTO(Map<String, Object> item) {
        return AccommodationDTO.builder()
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
        try {
            return obj != null ? Integer.parseInt(obj.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }




}
