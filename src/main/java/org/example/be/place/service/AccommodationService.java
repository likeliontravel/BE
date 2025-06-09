package org.example.be.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be.place.dto.AccommodationDTO;
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




    }



}
