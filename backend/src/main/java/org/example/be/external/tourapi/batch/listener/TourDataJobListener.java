package org.example.be.external.tourapi.batch.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.be.external.tourapi.batch.reader.TourApiItemReader;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

import lombok.extern.slf4j.Slf4j;

/**
 * tourDataRefreshJob 이 '완료' 와 '성공' 을 구분하도록 Step 별 실패율을 판정하는 리스너
 *
 * Step 이 예외 없이 끝나면 Spring Batch 는 Job 을 COMPLETED 로 기록한다. 그런데 이 배치는
 * - 지역 단위 수집 실패를 Reader 가 건너뛰고 (TourApiItemReader)
 * - 아이템 단위 실패를 skip 한다 (BatchConfig 의 faultTolerant 설정)
 * 그래서 대부분이 실패해도 COMPLETED 로 끝날 수 있었다. 이 리스너가 Job 이 끝난 직후 Step 별 실패율을 보고
 * 임계치 이상이면 Job 을 FAILED 로 바꾼다. 이미 수집, 저장된 데이터는 그대로 남는다.
 *
 * 판정 대상은 TourApiItemReader 를 쓰는 chunk Step(관광지, 식당, 숙박) 뿐이다.
 * Tasklet Step(지역, 카테고리 갱신)은 실패하면 예외로 Step 자체가 FAILED 가 되므로 여기서 볼 필요가 없다.
 *
 * status 까지 바꾸는 이유: exitStatus 만 바꾸면 BATCH_JOB_EXECUTION.STATUS 는 COMPLETED 로 남고,
 * TourDataScheduler 가 찍는 jobExecution.getStatus() 도 COMPLETED 가 된다.
 * Spring Batch 는 afterJob 이 끝난 뒤 JobExecution 을 저장하므로 여기서 바꾼 값이 그대로 기록된다.
 */
@Slf4j
public class TourDataJobListener implements JobExecutionListener {

	// 실패율 임계치(%). 이 값 이상이면 FAILED 로 판정한다.
	private static final int FAILURE_THRESHOLD_PERCENT = 30;

	@Override
	public void afterJob(JobExecution jobExecution) {
		// COMPLETED 가 아니면 이미 어떤 Step 이 예외로 실패했고, Spring Batch 가 그 원인을 ERROR + 스택으로 기록했다.
		if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
			return;
		}

		List<String> violations = new ArrayList<>();
		for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
			judge(stepExecution).ifPresent(violations::add);
		}

		if (violations.isEmpty()) {
			log.info("[TourDataJob] 실패율 판정 통과 (임계 {}%)", FAILURE_THRESHOLD_PERCENT);
			return;
		}

		String summary = String.join(" / ", violations);
		log.error("[TourDataJob] 실패율 임계 초과로 Job 을 FAILED 로 전환 - {}", summary);
		jobExecution.setStatus(BatchStatus.FAILED);
		jobExecution.setExitStatus(ExitStatus.FAILED.addExitDescription(summary));
	}

	/**
	 * Step 하나를 판정한다. 위반이면 사유를, 아니면 빈 값을 돌려준다.
	 * 수집 실패율은 지역 단위 (areaCode x contentTypeId), skip 률은 아이템 단위다.
	 * skip 률의 분모는 readCount 다 - 변경 없는 아이템(filterCount) 까지 포함해야 평시 비율이 왜곡되지 않는다.
	 */
	private Optional<String> judge(StepExecution stepExecution) {
		ExecutionContext context = stepExecution.getExecutionContext();
		if (!context.containsKey(TourApiItemReader.FETCH_TOTAL_COUNT_KEY)) {
			return Optional.empty();
		}

		String stepName = stepExecution.getStepName();
		int fetchTotal = context.getInt(TourApiItemReader.FETCH_TOTAL_COUNT_KEY);
		int fetchFailed = context.getInt(TourApiItemReader.FETCH_FAILED_COUNT_KEY);

		// 수집 대상 지역이 0개면 수집이 아예 일어나지 않은 것이다. 조용한 COMPLETED 를 막기 위해 실패로 본다.
		if (fetchTotal == 0) {
			return Optional.of(stepName + " 수집 대상 지역 0개");
		}

		if (fetchFailed * 100 / fetchTotal >= FAILURE_THRESHOLD_PERCENT) {
			return Optional.of(stepName + " 수집 실패 " + fetchFailed + "/" + fetchTotal);
		}

		long readCount = stepExecution.getReadCount();
		long skipCount = stepExecution.getSkipCount();

		if (readCount > 0 && skipCount * 100 / readCount >= FAILURE_THRESHOLD_PERCENT) {
			return Optional.of(stepName + " skip " + skipCount + "/" + readCount);
		}
		return Optional.empty();
	}
}
