package org.example.be.tour_api.service;

import org.example.be.place.theme.PlaceCategoryRepository;
import org.example.be.tour_api.dto.FetchResult;
import org.example.be.tour_api.util.CategoryClassifier;
import org.example.be.tour_api.util.TourApiClient;
import org.example.be.tour_api.util.TourApiParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PlaceCategory(카테고리 코드) 최신화 서비스
 *
 * TourAPI categoryCode2를 3단계(cat1 -> cat2 -> cat3)로 순회하며
 * contentTypeId별 (12, 14, 28, 32, 38, 39)로 PlaceCategory 테이블을 갱신한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshCategoryService {

	private final TourApiClient tourApiClient;
	private final TourApiParser tourApiParser;
	private final PlaceCategoryRepository placeCategoryRepository;
	private final CategoryClassifier categoryClassifier;

	@Value("${service-key}")
	private String serviceKey;

	// contentTypeId 목록: 12 - 관광지, 14 - 문화시설, 28 - 레포츠, 32 - 숙박, 38 - 쇼핑, 39 - 음식점
	private static final int[] CONTENT_TYPE_IDS = {12, 14, 28, 32, 38, 39};

	/**
	 * PlaceCategory 갱신 메인 로직
	 * contentTypeId별로 cat1 -> cat2 -> cat3 3단계 순회
	 */
	@Transactional
	public FetchResult refreshCategories() {
		try {
			int totalSaved = 0;
			int totalUpdated = 0;
			int totalSkipped = 0;

			for (int contentTypeId : CONTENT_TYPE_IDS) {
				String contentTypeIdStr = String.valueOf(contentTypeId);
				log.info("[RefreshCategory] contentTypeId={} 카테고리 조회 시작", contentTypeId);
			}

		}
	}











































}
