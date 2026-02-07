package org.example.be.tour_api.batch.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.example.be.place.region.TourRegionRepository;
import org.example.be.tour_api.util.TourApiClient;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TourAPI에서 데이터를 수집하는 ItemStreamReader 구현체
 *
 * Execution Context : Step이 어디까지 진행했는지를 기록하는 메모장. BATCH_STEP_EXECUTION_CONTEXT테이블에 json 형태로 저장됨. (여기에서는 currentIndex 정보가 들어감)
 * 재시작 시 executionContext에서 이전 진행 위치 복원
 *
 * - open(): TourAPI에서 전체 데이터를 수집하여 내부 List에 저장
 * - read(): List에서 아이템을 하나씩 꺼내서 Processor로 전달
 * - update(): 매 chunk 완료 시 진행 위치를 ExecutionContext에 저장 ( 재시작 시 시점을 알기 위해 )
 * - close(): Step 종료 시 메모리 정리 -> GC가 수거할 수 있도록
 *
 * contentTypeId를 생성자를 통해 받아 각 contentTypeId에 따라 한 Step에 재사용 할 수 있도록 함 - BatchConfig에서 @StepScope 빈으로 생성됨
 * ex) TouristSpot: 12, 14, 28, 38 / Restaurant: 39 / Accommodation: 32
 */
@Slf4j
@RequiredArgsConstructor
public class TourApiItemReader implements ItemStreamReader<Map<String, Object>> {

	private final TourApiClient tourApiClient;
	private final TourRegionRepository tourRegionRepository;
	private final String serviceKey;
	private final int[] contentTypeIds;
	private final int numOfRows;

	private List<Map<String, Object>> items;
	private int currentIndex;

	/**
	 *  Step 시작 시 호출 - TourAPI에서 전체 데이터를 미리 가져옴
	 *  재시작 시 ExecutionContext에서 이전 위치 복원
	 *
	 *  1. DB에서 모든 areaCode 조회
	 *  2. 각 areaCode별로 TourAPI 전체 페이지 순회
	 *  3. 결과를 내부 List에 저장
	 */
	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		log.info("[TourApiItemReader] 데이터 수집 시작 (contentTypeId: {})", Arrays.toString(contentTypeIds));
		List<String> areaCodes = tourRegionRepository.findDistinctAreaCode();
		items = new ArrayList<>();

		int totalAreas = areaCodes.size();
		int currentArea = 0;
		int failedCount = 0;

		for (String areaCode : areaCodes) {
			currentArea++;
			for (int contentTypeId : contentTypeIds) {
				try {
					int code = Integer.parseInt(areaCode);
					List<Map<String, Object>> pageItems =
						tourApiClient.fetchAllPagesForArea(code, contentTypeId, numOfRows, serviceKey);
					items.addAll(pageItems);
					log.info("[TourApiItemReader] areaCode({}/{}) contentTypeId={} 수집 완료 ({}건)",
						currentArea, totalAreas, contentTypeId, pageItems.size());
				} catch (Exception e) {
					failedCount++;
					log.error("[TourApiItemReader] areaCode={} contentTypeId={} 수집 실패 - 건너뜀",
						areaCode, contentTypeId);
					log.error("[TourApiItemReader] 실패 원인 : {}", e.getMessage());
				}
			}
		}

		if (failedCount > 0) {
			log.warn("[TourApiItemReader] ⚠️ 총 {}건 수집 실패", failedCount);
		}

		// 재시작 시 이전 진행 위치 복원
		currentIndex = executionContext.containsKey("currentIndex") ?
			executionContext.getInt("currentIndex") : 0;

		log.info("[TourApiItemReader] 전체 수집 완료 - 총 {}건, 시작 인덱스: {}", items.size(), currentIndex);
	}

	/**
	 * chunk가 아이템을 요청할 때마다 호출
	 * List에서 하나씩 꺼내 반환, 모두 반환하면 null(데이터 끝)
	 */
	@Override
	public Map<String, Object> read() {
		if (currentIndex < items.size()) {
			return items.get(currentIndex++);
		}
		return null;
	}

	/**
	 * 각 chunk 완료 시 호출 - 현재 진행 위치를 Execution에 저장
	 * Job 실패 후 재시작 시 이 값으로 위치 복원
	 */
	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		executionContext.putInt("currentIndex", currentIndex);
	}

	/**
	 * Step 종료 시 호출 - 메모리 정리
	 */
	@Override
	public void close() throws ItemStreamException {
		items = null;
		currentIndex = 0;

	}

}
