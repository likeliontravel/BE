package org.example.be.tour_api.scheduler;

import org.example.be.tour_api.dto.FetchResult;
import org.example.be.tour_api.service.RefreshRegionService;
import org.example.be.tour_api.service.TouristSpotFetchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TourDataScheduler {

	private final RefreshRegionService refreshRegionService;
	private final TouristSpotFetchService touristSpotFetchService;

	/**
	 * 매일 오전 6시에 Tour 데이터 갱신
	 * 1) TourRegion 테이블 갱신 (지역 코드 최신화)
	 * 2) TouristSpot 데이터 갱신 (여행지 정보 최신화)
	 *
	 * 1단계 실패 시 2단계 실행하지 않음
	 * - TourRegion이 최신이 아니면 TouristSpot 매칭이 실패하기 때문
	 */
	@Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
	public void refreshTourData() {
		log.info("========== [Scheduler] TourData Refresh start ==========");
		long startTime = System.currentTimeMillis();

		// 1단계 : TourRegion 갱신
		log.info("[Scheduler] 1 / 2 - TourRegion 갱신 시작");
		try {
			FetchResult regionResult = refreshRegionService.refreshRegions();
			log.info("[Scheduler] 1 / 2 - TourRegion 갱신 완료 - 신규: {}, 변경: {}, 무변경: {} ",
				regionResult.saved(), regionResult.updated(), regionResult.skipped());
		} catch (Exception e) {
			log.error("[Scheduler] TourRegion 갱신 실패 -> TouristSpot 갱신 중단", e);
			long elapsed = System.currentTimeMillis() - startTime;
			log.info("========== [Scheduler] Tour 데이터 갱신 중단 ({}ms) ==========", elapsed);
			return;
		}

		// 2단계 : TouristSpot 갱신
		log.info("[Scheduler] 2 / 2 TouristSpot 갱신 시작");
		try {
			FetchResult spotResult = touristSpotFetchService.fetchAllTourData(12, 9999);
			log.info("[Scheduler] 2 / 2 - TouristSpot 갱신 완료 - 신규: {}, 변경: {}, 무변경: {}, 실패: {}, 전체: {}",
				spotResult.saved(), spotResult.updated(), spotResult.skipped(), spotResult.failed(),
				spotResult.total());
		} catch (Exception e) {
			log.error("[Scheduler] 2 / 2 - TouristSpot 갱신 실패", e);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		log.info("========== [Scheduler] Tour 데이터 갱신 종료({}ms) ==========", elapsed);

	}

}
