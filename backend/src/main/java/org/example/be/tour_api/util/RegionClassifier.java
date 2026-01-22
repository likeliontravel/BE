package org.example.be.tour_api.util;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * TourAPI에서 받은 지역 정보를 Toleave 30개 서비스 지역으로 분류하는 유틸리티
 *
 * 서비스 지역 :
 * 시/군/구 : 가평, 양평, 강릉, 경주, 전주, 여수, 춘천, 홍천, 태안, 통영, 거제, 포항, 안동
 * 광역시 : 서울, 인천, 대전, 대구, 광주, 부산, 울산, 세종
 * 도 : 경기, 강원, 충북, 충남, 경북, 경남, 전북, 전남, 제주
 */

@Component
public class RegionClassifier {

	// 시/군/구 단위 키워드( siGunGu 우선 매칭 )
	private static final List<String> SIGUNGU_KEYWORDS = List.of(
		"가평", "양평", "강릉", "경주", "전주", "여수", "춘천", "홍천", "태안", "통영", "거제", "포항", "안동"
	);

	// 광역시 키워드
	private static final List<String> METRO_KEYWORDS = List.of(
		"서울", "인천", "대전", "대구", "광주", "부산", "울산", "세종"
	);

	// 도 단위 키워드 ( 나머지 areaName 그대로 가게 되는 키워드 )
	private static final Map<String, String> PROVINCE_MAP = Map.of(
		"경기", "경기",
		"강원", "강원",
		"전북", "전북",
		"제주", "제주",
		"충청북", "충북",
		"충청남", "충남",
		"경상북", "경북",
		"경상남", "경남",
		"전라남", "전남"
	);

	/**
	 * 지역 분류 메서드
	 * @param areaName area 이름
	 * @param siGunGuName 시군구 이름
	 * @return 분류된 서비스 지역 명 또는 "기타"
	 */
	public String classify(String areaName, String siGunGuName) {
		// 1. 예외 케이스들 정리
		// 해운대구 -> 부산 ( '대구'키워드 오매칭 방지 목적 )
		if (siGunGuName != null && siGunGuName.contains("해운대")) {
			return "부산";
		}
		// 경기도 광주시 -> 경기도 ( 광주 광역시와 구분 )
		if (areaName != null && areaName.contains("경기")
			&& siGunGuName != null && siGunGuName.contains("광주")) {
			return "경기";
		}

		// 2. siGunGuName에서 시/군/구 키워드 검색
		if (siGunGuName != null) {
			for (String keyword : SIGUNGU_KEYWORDS) {
				if (siGunGuName.contains(keyword)) {
					return keyword;
				}
			}
		}

		// 3. areaName에서 광역시 키워드 검색
		if (areaName != null) {
			for (String keyword : METRO_KEYWORDS) {
				if (areaName.contains(keyword)) {
					return keyword;
				}
			}
		}

		// 4. areaName에서 도 키워드 검색
		if (areaName != null) {
			for (Map.Entry<String, String> entry : PROVINCE_MAP.entrySet()) {
				if (areaName.contains(entry.getKey())) {
					return entry.getValue();
				}
			}
		}

		// 5. 매칭 없을 시 "기타"로 분류
		return "기타";

	}

}
