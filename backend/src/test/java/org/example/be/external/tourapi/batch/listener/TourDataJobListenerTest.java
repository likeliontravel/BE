package org.example.be.external.tourapi.batch.listener;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.example.be.external.tourapi.batch.reader.TourApiItemReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

// TourDataJobListener 단위 테스트.
// Job 이 끝난 뒤의 JobExecution / StepExecution 을 직접 조립해 afterJob 의 판정만 검증한다.
// (Reader 가 수집 집계를 제대로 남기는지는 TourApiItemReaderTest 가 검증한다)
//
// 임계치는 30% 이고 '이상' 이면 FAILED 다. 경계값 29 / 30 을 양쪽에서 고정한다.
@DisplayName("TourDataJobListener 실패율 판정 단위 테스트")
class TourDataJobListenerTest {

	private final TourDataJobListener tourDataJobListener = new TourDataJobListener();

	// JUnit 은 테스트 메서드마다 인스턴스를 새로 만들므로, 이 appender 도 테스트마다 비어 있는 상태로 시작한다.
	private final ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
	private final Logger listenerLogger = (Logger)LoggerFactory.getLogger(TourDataJobListener.class);

	@BeforeEach
	void attachLogAppender() {
		logAppender.start();
		listenerLogger.addAppender(logAppender);
	}

	@AfterEach
	void detachLogAppender() {
		listenerLogger.detachAppender(logAppender);
		logAppender.stop();
	}

	@Test
	@DisplayName("수집 실패율과 skip 률이 모두 30% 미만이면 COMPLETED 를 유지하고 통과 로그를 남긴다")
	void belowThreshold_keepsCompleted() {
		JobExecution jobExecution = completedJob();
		chunkStep(jobExecution, "touristSpotFetchStep", 100, 29, 100, 29);

		tourDataJobListener.afterJob(jobExecution);

		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());
		assertThat(eventsAt(Level.INFO)).singleElement()
			.extracting(ILoggingEvent::getFormattedMessage)
			.isEqualTo("[TourDataJob] 실패율 판정 통과 (임계 30%)");
		assertThat(eventsAt(Level.ERROR)).isEmpty();
	}

	@Test
	@DisplayName("수집 실패율이 정확히 30% 면 status 와 exitStatus 를 모두 FAILED 로 바꾼다")
	void fetchFailureAtThreshold_marksJobFailed() {
		JobExecution jobExecution = completedJob();
		chunkStep(jobExecution, "touristSpotFetchStep", 100, 30, 1000, 0);

		tourDataJobListener.afterJob(jobExecution);

		// exitStatus 만 바꾸면 BATCH_JOB_EXECUTION.STATUS 와 스케줄러 로그는 COMPLETED 로 남는다 - 둘 다 확인한다
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
		assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.FAILED.getExitCode());
		assertThat(jobExecution.getExitStatus().getExitDescription()).contains("touristSpotFetchStep 수집 실패 30/100");
		assertThat(eventsAt(Level.ERROR)).singleElement()
			.extracting(ILoggingEvent::getFormattedMessage)
			.isEqualTo("[TourDataJob] 실패율 임계 초과로 Job 을 FAILED 로 전환 - touristSpotFetchStep 수집 실패 30/100");
	}

	@Test
	@DisplayName("skip 률이 30% 면 FAILED 다 - 분모는 readCount 다")
	void skipRateAtThreshold_marksJobFailed() {
		JobExecution jobExecution = completedJob();
		chunkStep(jobExecution, "accommodationFetchStep", 17, 0, 10, 3);

		tourDataJobListener.afterJob(jobExecution);

		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
		assertThat(jobExecution.getExitStatus().getExitDescription()).contains("accommodationFetchStep skip 3/10");
	}

	@Test
	@DisplayName("수집 대상 지역이 0개면 수집이 일어나지 않은 것이므로 FAILED 다")
	void noFetchUnits_marksJobFailed() {
		JobExecution jobExecution = completedJob();
		chunkStep(jobExecution, "restaurantFetchStep", 0, 0, 0, 0);

		tourDataJobListener.afterJob(jobExecution);

		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
		assertThat(jobExecution.getExitStatus().getExitDescription()).contains("restaurantFetchStep 수집 대상 지역 0개");
	}

	@Test
	@DisplayName("가운데 Step 하나만 넘어도 FAILED 다 - 마지막 Step 이 정상이라고 가려지지 않는다")
	void middleStepViolation_isNotMaskedByLastStep() {
		JobExecution jobExecution = completedJob();
		chunkStep(jobExecution, "touristSpotFetchStep", 68, 0, 20000, 0);
		chunkStep(jobExecution, "restaurantFetchStep", 17, 6, 5000, 0);
		chunkStep(jobExecution, "accommodationFetchStep", 17, 0, 3000, 0);

		tourDataJobListener.afterJob(jobExecution);

		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
		assertThat(jobExecution.getExitStatus().getExitDescription())
			.contains("restaurantFetchStep 수집 실패 6/17")
			.doesNotContain("touristSpotFetchStep")
			.doesNotContain("accommodationFetchStep");
	}

	@Test
	@DisplayName("Reader 집계가 없는 Tasklet Step 은 판정하지 않는다")
	void taskletStepsWithoutFetchCounts_areIgnored() {
		JobExecution jobExecution = completedJob();
		// Tasklet Step 은 savedCount 등만 남기고 수집 집계 키가 없다. readCount 도 0 이다.
		StepExecution refreshRegionStep = jobExecution.createStepExecution("refreshRegionStep");
		refreshRegionStep.getExecutionContext().putInt("savedCount", 0);
		jobExecution.createStepExecution("refreshCategoryStep");
		chunkStep(jobExecution, "touristSpotFetchStep", 68, 0, 20000, 0);

		tourDataJobListener.afterJob(jobExecution);

		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
	}

	@Test
	@DisplayName("이미 FAILED 인 Job 은 건드리지 않는다 - Step 실패의 원래 사유를 덮어쓰지 않는다")
	void alreadyFailedJob_isLeftUntouched() {
		JobExecution jobExecution = new JobExecution(new JobInstance(1L, "tourDataRefreshJob"), new JobParameters());
		jobExecution.setStatus(BatchStatus.FAILED);
		jobExecution.setExitStatus(ExitStatus.FAILED.addExitDescription("원래 사유"));
		chunkStep(jobExecution, "touristSpotFetchStep", 100, 100, 0, 0);

		tourDataJobListener.afterJob(jobExecution);

		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
		assertThat(jobExecution.getExitStatus().getExitDescription()).isEqualTo("원래 사유");
		assertThat(logAppender.list).isEmpty();
	}

	private JobExecution completedJob() {
		JobExecution jobExecution = new JobExecution(new JobInstance(1L, "tourDataRefreshJob"), new JobParameters());
		jobExecution.setStatus(BatchStatus.COMPLETED);
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		return jobExecution;
	}

	// TourApiItemReader 를 쓰는 chunk Step 을 흉내 낸다. getSkipCount() 는 read + process + write skip 의 합이다.
	private void chunkStep(JobExecution jobExecution, String stepName,
		int fetchTotal, int fetchFailed, long readCount, long skipCount) {
		StepExecution stepExecution = jobExecution.createStepExecution(stepName);
		stepExecution.getExecutionContext().putInt(TourApiItemReader.FETCH_TOTAL_COUNT_KEY, fetchTotal);
		stepExecution.getExecutionContext().putInt(TourApiItemReader.FETCH_FAILED_COUNT_KEY, fetchFailed);
		stepExecution.setReadCount(readCount);
		stepExecution.setProcessSkipCount(skipCount);
	}

	private List<ILoggingEvent> eventsAt(Level level) {
		return logAppender.list.stream()
			.filter(event -> event.getLevel() == level)
			.toList();
	}
}
