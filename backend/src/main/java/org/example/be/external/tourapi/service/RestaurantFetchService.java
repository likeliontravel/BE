package org.example.be.external.tourapi.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.be.domain.place.restaurant.dto.RestaurantResBody;
import org.example.be.domain.place.restaurant.entity.Restaurant;
import org.example.be.domain.place.restaurant.repository.RestaurantRepository;
import org.example.be.external.tourapi.util.TourApiClient;
import org.example.be.external.tourapi.util.TourApiParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	public List<RestaurantResBody> getData(int areaCode, int contentTypeId, int numOfRows, int pageNo) {
		String rawJson = tourApiClient.fetchTourData(areaCode, contentTypeId, numOfRows, pageNo,
			serviceKey); //tourApiClient에서 정보에 맞는 데이터를 가져옴

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
				.title((String)item.get("title"))
				.addr1((String)item.get("addr1"))
				.addr2((String)item.get("addr2"))
				.areaCode(String.valueOf(item.get("areacode")))
				.siGunGuCode(String.valueOf(item.get("sigungucode")))
				.cat1((String)item.get("cat1"))
				.cat2((String)item.get("cat2"))
				.cat3((String)item.get("cat3"))
				.imageUrl((String)item.get("firstimage"))
				.thumbnailImageUrl((String)item.get("firstimage2"))
				.mapX(toDouble(item.get("mapx")))
				.mapY(toDouble(item.get("mapy")))
				.mLevel(toInteger(item.get("mlevel")))
				.tel((String)item.get("tel"))
				.modifiedTime((String)item.get("modifiedtime"))
				.createdTime((String)item.get("createdtime"))
				.build();

			restaurantRepository.save(restaurant);
		}
	}

	private RestaurantResBody toDTO(Map<String, Object> item) {
		return RestaurantResBody.builder()
			.contentId(String.valueOf(item.get("contentid")))
			.title((String)item.get("title"))
			.addr1((String)item.get("addr1"))
			.addr2((String)item.get("addr2"))
			.areaCode(String.valueOf(item.get("areacode")))
			.siGunGuCode(String.valueOf(item.get("sigungucode")))
			.cat1((String)item.get("cat1"))
			.cat2((String)item.get("cat2"))
			.cat3((String)item.get("cat3"))
			.imageUrl((String)item.get("firstimage"))
			.thumbnailImageUrl((String)item.get("firstimage2"))
			.mapX(toDouble(item.get("mapx")))
			.mapY(toDouble(item.get("mapy")))
			.mLevel(toInteger(item.get("mlevel")))
			.tel((String)item.get("tel"))
			.modifiedTime((String)item.get("modifiedtime"))
			.createdTime((String)item.get("createdtime"))
			.build();
	}

	private Double toDouble(Object obj) {
		if (obj == null || String.valueOf(obj).isBlank()) {
			return null;    // TourAPI 는 값이 없으면 "" 를 보낸다. 변환 실패가 아니라 '값 없음' 이므로 로그를 남기지 않는다.
		}

		try {
			return Double.parseDouble(String.valueOf(obj));
		} catch (NumberFormatException e) {
			// 좌표 (mapX, mapY) 가 여기서 null 이 되면 그 장소는 지도에 뜨지 않는다.
			// 건별 오류라 예외로 올리기엔 과하지만, 무음으로 두면 데이터 품질이 조용히 떨어진다.
			// 운영 로그 레벨이 INFO 라 debug 로 남기면 보이지 않으므로 WARN 으로 남긴다.
			log.warn("[TypeConvert] Double 변환 실패 - value={}", obj);
			return null;
		}
	}

	private Integer toInteger(Object obj) {
		if (obj == null || String.valueOf(obj).isBlank()) {
			return null;
		}

		try {
			return Integer.parseInt(String.valueOf(obj));
		} catch (NumberFormatException e) {
			// 사유는 toDouble() 의 같은 catch 주석 참고
			log.warn("[TypeConvert] Integer 변환 실패 - value={}", obj);
			return null;
		}
	}
}
