package org.example.be.tour_api.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.place.theme.PlaceCategory;
import org.example.be.place.theme.PlaceCategoryRepository;
import org.example.be.tour_api.dto.CategoryCodeDTO;
import org.example.be.tour_api.dto.FetchResult;
import org.example.be.tour_api.dto.SaveResult;
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

				// 1단계: cat1(대분류) 목록 조회
				String cat1Json = tourApiClient.fetchCategoryCodes(contentTypeIdStr, null, null, serviceKey);
				List<CategoryCodeDTO> cat1List = tourApiParser.parseCategories(cat1Json);

				for (CategoryCodeDTO cat1 : cat1List) {

					// 2단계: cat2(중분류) 목록 조회
					String cat2Json = tourApiClient.fetchCategoryCodes(contentTypeIdStr, cat1.code(), null, serviceKey);
					List<CategoryCodeDTO> cat2List = tourApiParser.parseCategories(cat2Json);

					for (CategoryCodeDTO cat2 : cat2List) {

						// 3단계: cat3(소분류) 목록 조회
						String cat3Json = tourApiClient.fetchCategoryCodes(contentTypeIdStr, cat1.code(), cat2.code(),
							serviceKey);
						List<CategoryCodeDTO> cat3List = tourApiParser.parseCategories(cat3Json);

						for (CategoryCodeDTO cat3 : cat3List) {

							// theme 분류
							String theme = categoryClassifier.classify(cat3.name(), cat2.name());

							// DB upsert 수행
							SaveResult result = upsertPlaceCategory(
								cat3.code(), contentTypeIdStr,
								cat1.code(), cat2.code(),
								cat1.name(), cat2.name(), cat3.name(),
								theme
							);
							switch (result) {
								case SAVED -> totalSaved++;
								case UPDATED -> totalUpdated++;
								case SKIPPED -> totalSkipped++;
							}
						}
					}
				}

				log.info("[RefreshCategory] contentTypeId={} 처리 완료", contentTypeId);
			}

			log.info("[RefreshCategory] 완료 - 신규: {}, 변경: {}, 무변경: {}", totalSaved, totalUpdated, totalSkipped);
			return new FetchResult(totalSaved, totalUpdated, totalSkipped, 0);

		} catch (Exception e) {
			log.error("[RefreshCategory] 카테고리 갱신 실패", e);
			throw new ResourceUpdateException("카테고리 정보 갱신 중 오류 발생", e);
		}
	}

	/**
	 * PlaceCategory upsert (변경 시에만 업데이트)
	 */
	private SaveResult upsertPlaceCategory(
		String cat3Code, String contentTypeId,
		String cat1Code, String cat2Code,
		String largeClassification, String midClassification, String smallClassification,
		String theme
	) {
		Optional<PlaceCategory> existing = placeCategoryRepository.findByCat3(cat3Code);

		if (existing.isPresent()) {
			PlaceCategory pc = existing.get();

			// 변경 여부 확인
			boolean changed =
				!Objects.equals(contentTypeId, pc.getContentTypeId())
					|| !Objects.equals(cat1Code, pc.getCat1())
					|| !Objects.equals(cat2Code, pc.getCat2())
					|| !Objects.equals(largeClassification, pc.getLargeClassification())
					|| !Objects.equals(midClassification, pc.getMidClassification())
					|| !Objects.equals(smallClassification, pc.getSmallClassification())
					|| !Objects.equals(theme, pc.getTheme());

			if (changed) {
				pc.setContentTypeId(contentTypeId);
				pc.setCat1(cat1Code);
				pc.setCat2(cat2Code);
				pc.setLargeClassification(largeClassification);
				pc.setMidClassification(midClassification);
				pc.setSmallClassification(smallClassification);
				pc.setTheme(theme);
				// JPA dirty checking으로 자동 업데이트
				return SaveResult.UPDATED;
			} else {
				return SaveResult.SKIPPED;
			}
		} else {
			PlaceCategory placeCategory = PlaceCategory.builder()
				.cat3(cat3Code)
				.contentTypeId(contentTypeId)
				.cat1(cat1Code)
				.cat2(cat2Code)
				.largeClassification(largeClassification)
				.midClassification(midClassification)
				.smallClassification(smallClassification)
				.theme(theme)
				.build();
			placeCategoryRepository.save(placeCategory);
			return SaveResult.SAVED;
		}
	}

}
