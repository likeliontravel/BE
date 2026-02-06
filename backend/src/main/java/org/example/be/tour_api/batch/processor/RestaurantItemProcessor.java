package org.example.be.tour_api.batch.processor;

import java.util.Map;
import java.util.Optional;

import org.example.be.place.region.TourRegion;
import org.example.be.place.restaurant.entity.Restaurant;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.example.be.place.theme.PlaceCategory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Restaurant 배치 처리를 위한 ItemProcessor
 *
 * 역할:
 * - TourAPI 응답 데이터(Map)를 Restaurant 엔티티로 변환
 * - TourRegion, PlaceCategory 연결 (2단계 fallback 로직)
 * - 기존 데이터 존재 시 변경 감지 (modifiedTime 기준)
 *
 * 반환값:
 * - Restaurant 엔티티: 신규 저장 또는 업데이트 대상 -> writer가 저장
 * - null: 변경 없음(SKIPPED) -> Spring Batch filterCount에 기록
 */
@Slf4j
@RequiredArgsConstructor
public class RestaurantItemProcessor implements ItemProcessor<Map<String, Object>, Restaurant>, StepExecutionListener {

	private final RestaurantRepository restaurantRepository;
	private final PlaceProcessorHelper processorHelper;

	// 로깅용 신규저장, 업데이트 카운트
	private int savedCount;
	private int updatedCount;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		savedCount = 0;
		updatedCount = 0;
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		stepExecution.getExecutionContext().putInt("savedCount", savedCount);
		stepExecution.getExecutionContext().putInt("updatedCount", updatedCount);
		return stepExecution.getExitStatus();
	}

	@Override
	public Restaurant process(Map<String, Object> item) throws Exception {
		String contentId = String.valueOf(item.get("contentid"));

		// TourRegion, PlaceCategory 매칭 (fallback 포함)
		TourRegion tourRegion = processorHelper.resolveTourRegion(item);
		PlaceCategory placeCategory = processorHelper.resolvePlaceCategory(item);

		// 기존 데이터 존재 여부 확인
		Optional<Restaurant> existing = restaurantRepository.findByContentId(contentId);

		if (existing.isPresent()) {
			Restaurant restaurant = existing.get();
			boolean changed = processorHelper.updateCommonFields(restaurant, item);
			if (changed) {
				restaurant.setTourRegion(tourRegion);
				restaurant.setPlaceCategory(placeCategory);
				updatedCount++;
				log.debug("[Processor] UPDATED Restaurant - contentId={}", contentId);
				return restaurant;
			} else {
				log.debug("[Processor] SKIPPED Restaurant - contentId={}", contentId);
				return null;
			}
		} else {
			// 신규 데이터 -> 새 엔티티 생성
			Restaurant newRestaurant = createNew(item, tourRegion, placeCategory);
			savedCount++;
			log.trace("[Processor] SAVED Restaurant - contentId={}", contentId);
			return newRestaurant;
		}
	}

	/**
	 * 신규 Restaurant 엔티티 생성
	 */
	private Restaurant createNew(Map<String, Object> item, TourRegion tourRegion, PlaceCategory placeCategory) {
		return Restaurant.builder()
			.contentId(String.valueOf(item.get("contentid")))
			.title(String.valueOf(item.get("title")))
			.addr1(String.valueOf(item.get("addr1")))
			.addr2(String.valueOf(item.get("addr2")))
			.areaCode(String.valueOf(item.get("areacode")))
			.siGunGuCode(processorHelper.getSiGunGuCode(item))
			.cat1(String.valueOf(item.get("cat1")))
			.cat2(String.valueOf(item.get("cat2")))
			.cat3(String.valueOf(item.get("cat3")))
			.imageUrl(String.valueOf(item.get("firstimage")))
			.thumbnailImageUrl(String.valueOf(item.get("firstimage2")))
			.mapX(processorHelper.toDouble(item.get("mapx")))
			.mapY(processorHelper.toDouble(item.get("mapy")))
			.mLevel(processorHelper.toInteger(item.get("mlevel")))
			.tel(String.valueOf(item.get("tel")))
			.modifiedTime(String.valueOf(item.get("modifiedtime")))
			.createdTime(String.valueOf(item.get("createdtime")))
			.tourRegion(tourRegion)
			.placeCategory(placeCategory)
			.build();
	}
}
