package org.example.be.tour_api.scheduler;

import java.time.LocalDateTime;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TourDataScheduler {

	private final JobLauncher jobLauncher;    // 내부 run() 메서드로 Job 실행
	private final Job tourDataRefreshJob;    // Bean으로 등록한 Job

	/**
	 * 매일 오전 6시에 Tour 데이터 갱신
	 * 1) TourRegion 테이블 갱신 (지역 코드 최신화)
	 * 2) TouristSpot 데이터 갱신 (여행지 정보 최신화)
	 * 3) Restaurant 데이터 갱신 (식당 정보 최신화)
	 * 4) Accommodation 데이터 갱신 (숙소 정보 최신화)
	 *
	 * 실행 이력은 Spring Batch 메타데이터 테이블에 자동 기록됨
	 *
	 * 1단계 실패 시 2단계 실행하지 않음
	 * - TourRegion이 최신이 아니면 TouristSpot 매칭이 실패하기 때문
	 *
	 * JobExecution, StepExecution 내부 구조
	 * JobExecution (Job 1회 실행의 전체 기록)
	 *   ├── status: COMPLETED / FAILED
	 *   ├── startTime, endTime
	 *   ├── jobParameters: { "runTime": "2026-02-06T06:00:00" }
	 *   ├── executionContext: Job 레벨 공유 데이터
	 *   └── stepExecutions: Collection<StepExecution>  ← 각 Step별 실행 기록
	 *       ├── StepExecution[0] (refreshRegionStep)
	 *       │   ├── status, startTime, endTime
	 *       │   ├── readCount, writeCount, filterCount, skipCount
	 *       │   └── executionContext: { "savedCount": 251, "updatedCount": 0, ... }
	 *       ├── StepExecution[1] (touristSpotFetchStep)
	 *       │   └── ...
	 *       └── ...
	 *   - executionContext는 Step 안에서 자유롭게 데이터를 저장/조회할 수 있는 Map형태의 저장소
	 *   - Processor에서 afterStep()으로 저장한 savedCount/updatedCount가 여기에 들어감
	 */
	@Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
	public void refreshTourData() {
		log.info("========== [Scheduler] TourData Refresh start ==========");
		long startTime = System.currentTimeMillis();

		try {
			JobParameters jobParameters = new JobParametersBuilder()    // 같은 Job + 같은 Parameters 조합이 COMPLETE상태라면 재실행 거부됨. -> 매일 스케줄러가 실행할 때마다 runTime키에 현재시각을 넣어서 매번 고유한 파라미터를 만들어야 재실행 가능.
				.addString("runTime",
					LocalDateTime.now().toString())
				.toJobParameters();    // Builder 패턴 마지막단계 - 내부에 쌓인 key-value 쌍을 불변 JobParameters객체로 변환

			JobExecution jobExecution = jobLauncher.run(tourDataRefreshJob, jobParameters);

			// 각 Step 결과 로깅
			int stepNumber = 1;
			int totalSteps = jobExecution.getStepExecutions().size();
			for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
				ExecutionContext ctx = stepExecution.getExecutionContext();

				int saved = ctx.containsKey("savedCount") ? ctx.getInt("savedCount") : 0;
				int updated = ctx.containsKey("updatedCount") ? ctx.getInt("updatedCount") : 0;

				int skipped;
				int failed;
				int readCount;    // chunk step에서 Reader가 읽어온 총 아이템 개수 (TourAPI에서 받아온 데이터 수), tasklet step에서는 readCount개념이 없어 직접 saved + updated + skipped + failed 계산.

				if (ctx.containsKey("skippedCount")) {
					//Tasklet Step (refreshRegionStep) - 모든 카운트가 ExecutionContext에 저장됨
					skipped = ctx.getInt("skippedCount");
					failed = ctx.getInt("failedCount");
					readCount = saved + updated + skipped + failed;
				} else {
					// ChunkStep - filterCount = 무변경, skipCount = 실패, readCount = API 수신
					skipped = (int)stepExecution.getFilterCount();
					failed = (int)stepExecution.getSkipCount();
					readCount = (int)stepExecution.getReadCount();
				}

				int total = saved + updated + skipped
					+ failed; // 처리 중 예외가 발생하면 readCount > total이 될 수 있음 (읽었지만 카운트에 반영되기 전 실패한 경우). 그래서 별도로 total을 계산해둠.

				log.info("[Scheduler] {}/{} - {} [{}] - API 수신: {}, 신규: {}, 변경: {}, 무변경: {}, 실패: {}, 전체: {}",
					stepNumber++, totalSteps, stepExecution.getStepName(), stepExecution.getStatus(), readCount, saved,
					updated, skipped, failed, total);
			}

			long elapsed = System.currentTimeMillis() - startTime;
			log.info("========== [Scheduler] TourData Batch Job {} ({}ms) ==========",
				jobExecution.getStatus(), elapsed);
		} catch (Exception e) {
			long elapsed = System.currentTimeMillis() - startTime;
			log.error("========== [Scheduler] TourData Batch Job 실패 ({}ms) ==========", elapsed, e);
		}
	}
}
