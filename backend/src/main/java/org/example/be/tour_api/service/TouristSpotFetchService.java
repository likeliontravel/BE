package org.example.be.tour_api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.be.place.region.TourRegion;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.place.theme.PlaceCategory;
import org.example.be.place.theme.PlaceCategoryRepository;
import org.example.be.place.touristSpot.dto.TouristSpotDTO;
import org.example.be.place.touristSpot.entity.TouristSpot;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.example.be.tour_api.dto.FetchResult;
import org.example.be.tour_api.dto.SaveResult;
import org.example.be.tour_api.util.TourApiClient;
import org.example.be.tour_api.util.TourApiParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TouristSpotFetchService {

	private final TouristSpotRepository touristSpotRepository;
	private final TourApiClient tourApiClient;
	private final TourApiParser tourApiParser;
	// 변경된 스키마에 따라 저장 시점에 연관관계를 완결내도록 조정하면서 추가
	private final TourRegionRepository tourRegionRepository;
	private final PlaceCategoryRepository placeCategoryRepository;

	@Value("${service-key}")
	private String serviceKey;

	public List<TouristSpotDTO> getTouristSpots(int areaCode, String state, int contentTypeId, int numOfRows,
		int pageNo) throws Exception {
		if (pageNo <= 0) {
			return getAllData(areaCode, state, contentTypeId, numOfRows);
		} else {
			return getPageData(areaCode, state, contentTypeId, numOfRows, pageNo);
		}
	}

	// 페이지 입력 시 fetch
	private List<TouristSpotDTO> getPageData(int areaCode, String state, int contentTypeId, int numOfRows,
		int pageNo) throws Exception {
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
	private List<TouristSpotDTO> getAllData(int areaCode, String state, int contentTypeId, int numOfRows) throws
		Exception {
		int pageNo = 1;
		List<TouristSpotDTO> allItems = new ArrayList<>();

		while (true) {
			String json = tourApiClient.fetchTourData(areaCode, contentTypeId, numOfRows, pageNo, serviceKey);
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

			if (filtered.isEmpty())
				break;

			filtered.forEach(this::saveIfNotExist);
			allItems.addAll(filtered.stream().map(this::toDTO).toList());
			pageNo++;
		}

		return allItems;
	}

	// contentId로 조회 시 없는 경우 ( 기존에 없던 장소일 경우 ) 새로 저장
	private void saveIfNotExist(Map<String, Object> item) {
		String contentId = String.valueOf(item.get("contentid"));

		// 이미 존재하는 장소인지 검증 (없는 경우만 저장)
		if (touristSpotRepository.existsByContentId(contentId))
			return;

		String areaCode = String.valueOf(item.get("areacode"));
		Object rawSigungu = item.get("sigungucode");
		String siGunGuCode =
			rawSigungu != null && !String.valueOf(rawSigungu).isBlank() ? String.valueOf(rawSigungu) : "99";

		String cat3 = (String)item.get("cat3");

		log.warn(
			"[RegionMatch] contentId={}, title={}, areaCode={}, sigunguCode={}",
			item.get("contentid"),
			item.get("title"),
			item.get("areacode"),
			item.get("sigungucode")
		);

		TourRegion tourRegion = tourRegionRepository
			.findByAreaCodeAndSiGunGuCode(areaCode, siGunGuCode)
			.orElseThrow(() -> new IllegalArgumentException("TourRegion 매칭 실패: " + areaCode + " " + siGunGuCode));

		PlaceCategory placeCategory = placeCategoryRepository
			.findByCat3(cat3)
			.orElseThrow(() -> new IllegalArgumentException("PlaceCategory 매칭 실패: " + cat3));

		TouristSpot touristSpot = TouristSpot.builder()
			.contentId(contentId)
			.title((String)item.get("title"))
			.addr1((String)item.get("addr1"))
			.addr2((String)item.get("addr2"))
			.areaCode(areaCode)
			.siGunGuCode(siGunGuCode)
			.cat1((String)item.get("cat1"))
			.cat2((String)item.get("cat2"))
			.cat3(cat3)
			.imageUrl((String)item.get("firstimage"))
			.thumbnailImageUrl((String)item.get("firstimage2"))
			.mapX(toDouble(item.get("mapx")))
			.mapY(toDouble(item.get("mapy")))
			.mLevel(toInteger(item.get("mlevel")))
			.tel((String)item.get("tel"))
			.modifiedTime((String)item.get("modifiedtime"))
			.createdTime((String)item.get("createdtime"))
			.tourRegion(tourRegion)
			.placeCategory(placeCategory)
			.build();

		touristSpotRepository.save(touristSpot);
	}

	/**
	 * 모든 지역의 여행지 데이터를 순회하며 저장 / 업데이트
	 * - TourRegion 테이블의 모든 areaCode 조회
	 * - 각 areaCode별로 TourAPI의 전체 페이지를 조회
	 * - 개별 item 실패 시 해당 item만 스킵, 전체 롤백 안함
	 *
	 * @param contentTypeId 관광 타입 (12 : 관광지, 14: 문화시설 등)
	 * @param numOfRows 페이지 당 조회 건수
	 * @return FetchResult 저장 / 업데이트 / 스킵 / 실패 건수 집계
	 */
	@Transactional
	public FetchResult fetchAllTourData(int contentTypeId, int numOfRows) {
		List<String> areaCodes = tourRegionRepository.findDistinctAreaCode();

		int totalSaved = 0;
		int totalUpdated = 0;
		int totalSkipped = 0;
		int totalFailed = 0;

		int totalAreas = areaCodes.size();
		int currentArea = 0;

		for (String areaCode : areaCodes) {
			currentArea++;
			try {
				int code = Integer.parseInt(areaCode);
				log.info("[FetchAll] areaCode=({}/{}) 시작", currentArea, totalAreas);

				List<Map<String, Object>> items =
					tourApiClient.fetchAllPagesForArea(code, contentTypeId, numOfRows, serviceKey);

				for (Map<String, Object> item : items) {
					try {
						SaveResult result = saveOrUpdate(item);
						switch (result) {
							case SAVED -> totalSaved++;
							case UPDATED -> totalUpdated++;
							case SKIPPED -> totalSkipped++;
						}
					} catch (Exception e) {
						log.error("[FetchAll] 저장 실패 - contentId={}", item.get("contentid"), e);
						totalFailed++;
					}
				}
				log.info("[FetchAll] areaCode=({}/{}) 완료 ({}건)", currentArea, totalAreas, items.size());

			} catch (Exception e) {
				log.error("[FetchAll] areaCode={} 처리 실패", areaCode, e);
			}
		}

		log.info("[FetchAll] 전체 완료 - 신규: {}, 업데이트: {}, 스킵: {}, 실패: {}",
			totalSaved, totalUpdated, totalSkipped, totalFailed);

		return new FetchResult(totalSaved, totalUpdated, totalSkipped, totalFailed);
	}

	/**
	 * contentId로 기존 데이터 여부를 확인
	 * - 있으면 -> 변경 감지 후 업데이트, return UPDATED or SKIPPED
	 * - 없으면 -> 신규 저장, return SAVED
	 */
	private SaveResult saveOrUpdate(Map<String, Object> item) {
		String contentId = String.valueOf(item.get("contentid"));

		TourRegion tourRegion = resolveTourRegion(item);
		PlaceCategory placeCategory = resolvePlaceCategory(item);

		Optional<TouristSpot> existing = touristSpotRepository.findByContentId(contentId);
		if (existing.isPresent()) {
			boolean changed = updateIfChanged(existing.get(), item, tourRegion, placeCategory);
			return changed ? SaveResult.UPDATED : SaveResult.SKIPPED;
		} else {
			saveNew(item, tourRegion, placeCategory);
			return SaveResult.SAVED;
		}
	}

	/**
	 * 변경 감지 후 업데이트
	 * modifiedTime 기준 변경 여부 판단
	 * 변경된 경우에만 필드를 업데이트 (JPA dirty checking)
	 * @param existing : 이미 존재하던 여행지 정보, item : 새로 받아 파싱한 정보, tourRegion : 분류된 region을 포함한 TourRegion 엔티티, placeCategory : 분류된 theme를 포함한 PlaceCategory 엔티티
	 * @return 변경 발생 시 true, 동일하면 false
	 */
	private boolean updateIfChanged(
		TouristSpot existing,
		Map<String, Object> item,
		TourRegion tourRegion,
		PlaceCategory placeCategory) {

		String newModifiedTime = String.valueOf(item.get("modifiedtime"));
		if (!Objects.equals(existing.getModifiedTime(), newModifiedTime)) {
			existing.setTitle(String.valueOf(item.get("title")));
			existing.setAddr1(String.valueOf(item.get("addr1")));
			existing.setAddr2(String.valueOf(item.get("addr2")));
			existing.setAreaCode(String.valueOf(item.get("areacode")));
			existing.setSiGunGuCode(getSiGunGuCode(item));    // 이미 item으로 뽑히면서 String.valueOf()로 캐스팅 되어있음
			existing.setCat1(String.valueOf(item.get("cat1")));
			existing.setCat2(String.valueOf(item.get("cat2")));
			existing.setCat3(String.valueOf(item.get("cat3")));
			existing.setImageUrl(String.valueOf(item.get("firstimage")));
			existing.setThumbnailImageUrl(String.valueOf(item.get("firstimage2")));
			existing.setMapX(toDouble(item.get("mapx")));
			existing.setMapY(toDouble(item.get("mapy")));
			existing.setMLevel(toInteger(item.get("mlevel")));
			existing.setTel(String.valueOf(item.get("tel")));
			existing.setModifiedTime(newModifiedTime);
			existing.setTourRegion(tourRegion);
			existing.setPlaceCategory(placeCategory);
			log.debug("[TouristSpot Updated] contentId={}", existing.getContentId());
			return true;
		}
		return false;
	}

	/**
	 * 기존 정보가 없어 contentId가 조회되지 않음 ->
	 * 새로운 여행지 데이터 저장
	 * @param item : 새로 저장할 파싱된 여행지 정보, tourRegion : 분류된 region을 포함한 TourRegion 엔티티, placeCategory : 분류된 theme를 포함한 PlaceCategory 엔티티
	 */
	private void saveNew(Map<String, Object> item, TourRegion tourRegion, PlaceCategory placeCategory) {
		String contentId = String.valueOf(item.get("contentid"));
		String areaCode = String.valueOf(item.get("areacode"));
		String siGunGuCode = getSiGunGuCode(item);

		TouristSpot touristSpot = TouristSpot.builder()
			.contentId(contentId)
			.title(String.valueOf(item.get("title")))
			.addr1(String.valueOf(item.get("addr1")))
			.addr2(String.valueOf(item.get("addr2")))
			.areaCode(areaCode)
			.siGunGuCode(siGunGuCode)
			.cat1(String.valueOf(item.get("cat1")))
			.cat2(String.valueOf(item.get("cat2")))
			.cat3(String.valueOf(item.get("cat3")))
			.imageUrl(String.valueOf(item.get("firstimage")))
			.thumbnailImageUrl(String.valueOf(item.get("firstimage2")))
			.mapX(toDouble(item.get("mapx")))
			.mapY(toDouble(item.get("mapy")))
			.mLevel(toInteger(item.get("mlevel")))
			.tel(String.valueOf(item.get("tel")))
			.modifiedTime(String.valueOf(item.get("modifiedtime")))
			.createdTime(String.valueOf(item.get("createdtime")))
			.tourRegion(tourRegion)
			.placeCategory(placeCategory)
			.build();

		touristSpotRepository.save(touristSpot);
		log.debug("[TouristSpot Saved] contentId={}", touristSpot.getContentId());
	}

	// TouristSpot -> DTO 변환
	private TouristSpotDTO toDTO(Map<String, Object> item) {
		return TouristSpotDTO.builder()
			.contentId((String)item.get("contentid"))
			.title((String)item.get("title"))
			.addr1((String)item.get("addr1"))
			.addr2((String)item.get("addr2"))
			.areaCode((String)item.get("areacode"))
			.siGunGuCode((String)item.get("sigungucode"))
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

	// 응답받은 json에서 sigungucode값만 안전하게 추출. 만약 빈 값이나 null이면 99(기타) 반환
	private String getSiGunGuCode(Map<String, Object> item) {
		Object raw = item.get("sigungucode");
		return (raw != null && !String.valueOf(raw).isBlank())
			? String.valueOf(raw)
			: "99";
	}

	/**
	 * json에서 TourRegion 매칭
	 * - 1차 : 정확한 areaCode + siGunGuCode 매칭
	 * - 2차 : 같은 areaCode의  "기타" (siGunGuCode=99) 매칭
	 *
	 * @param item TourAPI에서 파싱된 여행지 데이터
	 * @return 매칭된 TourRegion 엔티티
	 * @throws IllegalStateException 매칭 실패 시
	 */
	private TourRegion resolveTourRegion(Map<String, Object> item) {
		String areaCode = String.valueOf(item.get("areacode"));
		String siGunGuCode = getSiGunGuCode(item);

		// 1차 : 정확한 매칭 시도
		Optional<TourRegion> exact = tourRegionRepository.findByAreaCodeAndSiGunGuCode(areaCode, siGunGuCode);
		if (exact.isPresent()) {
			return exact.get();
		}

		// 2차 : 같은 areaCode의 "기타"로 분류 결정
		log.warn("[TourRegion Fallback] areaCode={}, siGunGuCode={} -> 같은 지역 기타 분류 발생", areaCode, siGunGuCode);
		return tourRegionRepository.findByAreaCodeAndSiGunGuCode(areaCode, "99")
			.orElseThrow(() -> new IllegalStateException("TourRegion 매칭 실패 - areaCode: " + areaCode));
	}

	/**
	 * json에서 PlaceCategory 매칭
	 * - 1차 : cat3코드로 정확한 매칭
	 * - 2차 : theme="기타"인 카테고리로 fallback
	 *
	 * @param item TourAPI에서 파싱된 여행지 데이터
	 * @return 매칭된 PlaceCategory 엔티티
	 * @throws IllegalStateException 매칭 실패 시
	 */
	private PlaceCategory resolvePlaceCategory(Map<String, Object> item) {
		String cat3 = String.valueOf(item.get("cat3"));

		// 1차 : cat3로 정확한 매칭
		if (cat3 != null && !cat3.isBlank() && !"null".equals(cat3)) {
			Optional<PlaceCategory> exact = placeCategoryRepository.findByCat3(cat3);
			if (exact.isPresent()) {
				return exact.get();
			}
		}

		// 2차 : theme="기타"로 fallback
		log.warn("[PlaceCategory Fallback] cat3={} -> 기타", cat3);
		return placeCategoryRepository.findFirstByTheme("기타")
			.orElseThrow(() -> new IllegalStateException("PlaceCategory 매칭 실패 - 기타 테마 없음"));
	}

}