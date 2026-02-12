package org.example.be.tour_api.batch.processor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.example.be.place.entity.Place;
import org.example.be.place.region.TourRegion;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.place.theme.PlaceCategory;
import org.example.be.place.theme.PlaceCategoryRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Place 계열 엔티티 (TouristSpot, Restaurant, Accommodation)의
 * Batch Processor에서 공통으로 사용하는 로직 헬퍼 메서드로 빼서 모은 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceProcessorHelper {

	private final TourRegionRepository tourRegionRepository;
	private final PlaceCategoryRepository placeCategoryRepository;

	/**
	 * TourRegion 매칭 (2단계 fallback)
	 * 1차: 정확한 areaCode + siGunGuCode 매칭
	 * 2차: 같은 areaCode의 "기타"(siGunGuCode=99)로 분류 결정
	 */
	public TourRegion resolveTourRegion(Map<String, Object> item) {
		String areaCode = String.valueOf(item.get("areacode"));
		String siGunGuCode = getSiGunGuCode(item);

		Optional<TourRegion> exact = tourRegionRepository.findByAreaCodeAndSiGunGuCode(areaCode, siGunGuCode);

		if (exact.isPresent()) {
			return exact.get();
		}

		log.warn("[TourRegion Fallback] areaCode={}, siGunGuCode={} -> 같은 지역 기타로 분류",
			areaCode, siGunGuCode);
		return tourRegionRepository.findByAreaCodeAndSiGunGuCode(areaCode, "99")
			.orElseThrow(() -> new IllegalStateException("TourRegion 매칭 실패 - areaCode: " + areaCode));
	}

	/**
	 * PlaceCategory 매칭 (2단계 fallback)
	 * 1차: cat3 코드로 매칭
	 * 2차: theme="기타"로 분류 결정
	 */
	public PlaceCategory resolvePlaceCategory(Map<String, Object> item) {
		String cat3 = String.valueOf(item.get("cat3"));

		if (cat3 != null && !cat3.isBlank() && !"null".equals(cat3)) {
			Optional<PlaceCategory> exact = placeCategoryRepository.findByCat3(cat3);
			if (exact.isPresent()) {
				return exact.get();
			}
		}

		log.warn("[PlaceCategory Fallback] cat3={} -> 기타로 분류", cat3);
		return placeCategoryRepository.findFirstByTheme("기타")
			.orElseThrow(() -> new IllegalStateException("PlaceCategory 매칭 실패 - 기타 테마 없음"));
	}

	/**
	 * sigungucode 안전추출 (null/빈 값이면 "99"만 반환)
	 */
	public String getSiGunGuCode(Map<String, Object> item) {
		Object raw = item.get("sigungucode");
		return (raw != null && !String.valueOf(raw).isBlank())
			? String.valueOf(raw)
			: "99";
	}

	/**
	 * Place 공통 필드 업데이트 (TouristSpot, Restaurant, Accommodation 공용)
	 * modifiedTime이 다르면 변경된 것으로 판단
	 *
	 * @return 변경 발생 시 true, 동일하면 false
	 */
	public boolean updateCommonFields(
		Place existing,
		Map<String, Object> item
	) {
		String newModifiedTime = String.valueOf(item.get("modifiedtime"));

		if (!Objects.equals(existing.getModifiedTime(), newModifiedTime)) {
			existing.setTitle(String.valueOf(item.get("title")));
			existing.setAddr1(String.valueOf(item.get("addr1")));
			existing.setAddr2(String.valueOf(item.get("addr2")));
			existing.setAreaCode(String.valueOf(item.get("areacode")));
			existing.setSiGunGuCode(getSiGunGuCode(item));
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
			return true;
		}
		return false;
	}

	public Double toDouble(Object obj) {
		try {
			return obj != null ? Double.parseDouble(obj.toString()) : null;
		} catch (Exception e) {
			return null;
		}
	}

	public Integer toInteger(Object obj) {
		try {
			return obj != null ? Integer.parseInt(obj.toString()) : null;
		} catch (Exception e) {
			return null;
		}
	}

}
