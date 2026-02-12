package org.example.be.tour_api.util;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * PlaceCategory의 theme를 분류하는 유틸리티
 *
 * 기존 파이썬 스크립트의 classify_theme() 함수를 java로 이식
 * smallClassification(소분류명)과 midClassification(중분류명)을 키워드 매칭하여
 * 6개 테마 중 하나로 분류한다.
 *
 * 분류 우선순위:
 * 1. 체험 및 액티비티
 * 2. 자연 속에서 힐링
 * 3. 열정적인 쇼핑투어
 * 4. 미식 여행 및 먹방 중심
 * 5. 문화예술 및 역사탐방
 * 6. 기타 (fallback)
 */
@Component
public class CategoryClassifier {

	// 체험 및 액티비티 - 소분류 키워드
	private static final List<String> ACTIVITY_SMALL = List.of(
		"레포츠", "체험", "액티비티", "스포츠", "수상", "산악", "골프", "서핑", "승마",
		"낚시", "캠핑", "카트", "래프팅", "스키", "수영", "수상레저", "등산", "하이킹",
		"트레킹", "걷기", "자전거", "드라이브", "보트", "카약", "패러글라이딩", "암벽",
		"빙벽", "바이크", "헬스", "유람선", "잠수함"
	);

	// 체험 및 액티비티 - 중분류 키워드
	private static final List<String> ACTIVITY_MID = List.of(
		"레포츠", "체험", "스포츠"
	);

	// 자연 속에서 힐링 - 소분류 키워드
	private static final List<String> NATURE_SMALL = List.of(
		"자연", "힐링", "공원", "산", "계곡", "호수", "정원", "폭포", "수목원", "약수터",
		"해안절경", "해수욕장", "섬", "동굴", "강", "해변", "정상", "초원", "목장", "나무",
		"숲", "들판", "바위", "계곡물", "수로", "폭포수", "천연", "식물", "기암괴석"
	);

	// 자연 속에서 힐링 - 중분류 키워드
	private static final List<String> NATURE_MID = List.of(
		"자연관광지", "휴양림", "생태공원"
	);

	// 열정적인 쇼핑투어 - 소분류 키워드
	private static final List<String> SHOPPING_SMALL = List.of(
		"쇼핑", "시장", "패션", "상가", "아울렛", "면세점", "백화점", "매장", "부티크",
		"플리마켓", "잡화", "기념품", "수공예품", "상점", "토산품", "쇼핑몰", "상권"
	);

	// 열정적인 쇼핑투어 - 중분류 키워드
	private static final List<String> SHOPPING_MID = List.of(
		"상업시설", "쇼핑", "전통시장", "백화점", "면세점"
	);

	// 미식 여행 및 먹방 중심 - 소분류 키워드
	private static final List<String> FOOD_SMALL = List.of(
		"음식", "맛집", "카페", "전통찻집", "식당", "주점", "레스토랑", "술집", "포장마차",
		"펍", "바", "디저트", "베이커리", "전통주", "간식", "먹거리", "야식"
	);

	// 미식 여행 및 먹방 중심 - 중분류 키워드
	private static final List<String> FOOD_MID = List.of(
		"음식점", "식음료"
	);

	// 문화예술 및 역사탐방 - 소분류 키워드
	private static final List<String> CULTURE_SMALL = List.of(
		"박물관", "미술관", "전통문화", "역사", "유적지", "문화재", "기념관", "사찰", "고택",
		"민속", "기념비", "성곽", "고궁", "유물", "전시장", "전시회", "고대", "무덤", "비석", "전승관",
		"유교", "궁궐", "전통", "유교문화"
	);

	// 문화예술 및 역사탐방 - 중분류 키워드
	private static final List<String> CULTURE_MID = List.of(
		"역사관광지", "문화시설", "기념관", "사찰", "종교성지", "박물관", "미술관", "전시관"
	);

	/**
	 * 소분류명과 중분류명을 기반으로 theme 분류
	 *
	 * @param smallClassification 소분류명 (cat3 name)
	 * @param midClassification 중분류명 (cat2 name)
	 * @return 분류된 theme 명
	 */
	public String classify(String smallClassification, String midClassification) {
		String small = (smallClassification != null) ? smallClassification : "";
		String mid = (midClassification != null) ? midClassification : "";

		// 1. 체험 및 액티비티
		if (containsAnyKeyword(small, ACTIVITY_SMALL) || containsAnyKeyword(mid, ACTIVITY_MID)) {
			return "체험 및 액티비티";
		}

		// 2. 자연 속에서 힐링
		if (containsAnyKeyword(small, NATURE_SMALL) || containsAnyKeyword(mid, NATURE_MID)) {
			return "자연 속에서 힐링";
		}

		// 3. 열정적인 쇼핑투어
		if (containsAnyKeyword(small, SHOPPING_SMALL) || containsAnyKeyword(mid, SHOPPING_MID)) {
			return "열정적인 쇼핑투어";
		}

		// 4. 미식 여행 및 먹방 중심
		if (containsAnyKeyword(small, FOOD_SMALL) || containsAnyKeyword(mid, FOOD_MID)) {
			return "미식 여행 및 먹방 중심";
		}

		// 5. 문화에술 및 역사탐방
		if (containsAnyKeyword(small, CULTURE_SMALL) || containsAnyKeyword(mid, CULTURE_MID)) {
			return "문화예술 및 역사탐방";
		}

		// 6. 기타 (fallback)
		return "기타";
	}

	/**
	 * 텍스트에 키워드 목록 중 하나라도 포함되어 있는지 확인
	 */
	private boolean containsAnyKeyword(String text, List<String> keywords) {
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		return false;
	}
}
