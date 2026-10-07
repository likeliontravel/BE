package org.example.be.external.tourapi.batch.processor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.example.be.domain.place.region.TourRegion;
import org.example.be.domain.place.region.TourRegionRepository;
import org.example.be.domain.place.shared.entity.Place;
import org.example.be.domain.place.theme.PlaceCategory;
import org.example.be.domain.place.theme.PlaceCategoryRepository;
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
			existing.updateCommonFields(
				String.valueOf(item.get("title")),
				String.valueOf(item.get("addr1")),
				String.valueOf(item.get("addr2")),
				String.valueOf(item.get("areacode")),
				getSiGunGuCode(item),
				String.valueOf(item.get("cat1")),
				String.valueOf(item.get("cat2")),
				String.valueOf(item.get("cat3")),
				String.valueOf(item.get("firstimage")),
				String.valueOf(item.get("firstimage2")),
				toDouble(item.get("mapx")),
				toDouble(item.get("mapy")),
				toInteger(item.get("mlevel")),
				String.valueOf(item.get("tel")),
				newModifiedTime
			);
			return true;
		}
		return false;
	}

	public Double toDouble(Object obj) {
		if (obj == null || String.valueOf(obj).isBlank()) {
			return null;
		}

		try {
			return Double.parseDouble(String.valueOf(obj));
		} catch (NumberFormatException e) {
			// 좌표 (mapx, mapy)가 여기서 null이 되면 그 장소는 지도에 뜨지 않는다.
			// 건별 오류라 예외로 올리기엔 과하지만, 무음으로 두면 데이터 품질이 조용히 떨어진다.
			// 운영 로그 레벨이 INFO 라 debug 로 남기면 보이지 않으므로 WARN 으로 남긴다.
			log.warn("[TypeConvert] Double 변환 실패 - value={}", obj);
			return null;
		}
	}

	public Integer toInteger(Object obj) {
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
