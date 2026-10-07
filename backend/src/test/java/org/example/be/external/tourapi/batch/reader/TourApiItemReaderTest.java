package org.example.be.external.tourapi.batch.reader;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.example.be.domain.place.region.TourRegionRepository;
import org.example.be.external.tourapi.util.TourApiClient;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.CommonErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;

// TourApiItemReader 단위 테스트.
// 수집 단위(areaCode x contentTypeId) 실패를 '건너뛰되 기록·집계하는지', 그 외 예외는 '잡지 않고 전파하는지'를 검증한다.
// open() 의 예외는 Spring Batch 의 skip 대상이 아니므로(Step 즉시 FAILED), 수집 실패는 Reader 가 직접 세어 ExecutionContext 에 남긴다.
// 그 값으로 실패율을 판정하는 쪽은 TourDataJobListenerTest 가 검증한다.
@DisplayName("TourApiItemReader 수집 실패 기록·집계 단위 테스트")
@ExtendWith(MockitoExtension.class)
class TourApiItemReaderTest {

	private static final String SERVICE_KEY = "test-service-key";
	private static final int NUM_OF_ROWS = 9999;

	@Mock
	private TourApiClient tourApiClient;

	@Mock
	private TourRegionRepository tourRegionRepository;

	// JUnit 은 테스트 메서드마다 인스턴스를 새로 만들므로, 이 appender 도 테스트마다 비어 있는 상태로 시작한다.
	private final ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
	private final Logger readerLogger = (Logger)LoggerFactory.getLogger(TourApiItemReader.class);

	@BeforeEach
	void attachLogAppender() {
		logAppender.start();
		readerLogger.addAppender(logAppender);
	}

	@AfterEach
	void detachLogAppender() {
		readerLogger.detachAppender(logAppender);
		logAppender.stop();
	}

	@Test
	@DisplayName("일부 단위가 실패해도 계속 진행하고, 실패를 스택과 함께 기록한 뒤 집계를 남긴다")
	void partialFetchFailure_continuesAndRecordsCounts() {
		Map<String, Object> itemA = Map.of("contentid", "A");
		Map<String, Object> itemB = Map.of("contentid", "B");
		Map<String, Object> itemC = Map.of("contentid", "C");
		when(tourRegionRepository.findDistinctAreaCode()).thenReturn(List.of("1", "2"));
		when(tourApiClient.fetchAllPagesForArea(1, 12, NUM_OF_ROWS, SERVICE_KEY)).thenReturn(List.of(itemA));
		when(tourApiClient.fetchAllPagesForArea(1, 14, NUM_OF_ROWS, SERVICE_KEY)).thenReturn(List.of(itemB));
		when(tourApiClient.fetchAllPagesForArea(2, 12, NUM_OF_ROWS, SERVICE_KEY)).thenReturn(List.of(itemC));
		when(tourApiClient.fetchAllPagesForArea(2, 14, NUM_OF_ROWS, SERVICE_KEY))
			.thenThrow(fetchFailure("areaCode: 2, contentTypeId: 14"));

		TourApiItemReader reader = createReader(new int[] {12, 14});
		ExecutionContext executionContext = new ExecutionContext();
		reader.open(executionContext);

		// 수집 단위 = 지역 2 x 타입 2 = 4, 그중 1 실패
		assertThat(executionContext.getInt(TourApiItemReader.FETCH_TOTAL_COUNT_KEY)).isEqualTo(4);
		assertThat(executionContext.getInt(TourApiItemReader.FETCH_FAILED_COUNT_KEY)).isEqualTo(1);

		// 성공한 단위의 아이템만 수집 순서대로 읽힌다
		assertThat(reader.read()).isEqualTo(itemA);
		assertThat(reader.read()).isEqualTo(itemB);
		assertThat(reader.read()).isEqualTo(itemC);
		assertThat(reader.read()).isNull();

		// 실패 1건 = ERROR 1줄 + 스택(C5). 예전처럼 e.getMessage() 만 남기면 throwableProxy 가 null 이다.
		List<ILoggingEvent> errorEvents = eventsAt(Level.ERROR);
		assertThat(errorEvents).hasSize(1);
		ILoggingEvent errorEvent = errorEvents.get(0);
		assertThat(errorEvent.getFormattedMessage())
			.isEqualTo("[TourApiItemReader] areaCode=2, contentTypeId=14 수집 실패 - 건너뜀");
		IThrowableProxy throwableProxy = errorEvent.getThrowableProxy();
		assertThat(throwableProxy).isNotNull();
		assertThat(throwableProxy.getClassName()).isEqualTo(BusinessException.class.getName());
		// 원인(I/O 등)까지 남아야 운영에서 실패 사유를 알 수 있다
		assertThat(throwableProxy.getCause()).isNotNull();
		assertThat(throwableProxy.getCause().getMessage()).isEqualTo("Connection refused");

		// 요약 WARN 은 '실패 / 전체' 단위 수를 보여준다
		List<ILoggingEvent> warnEvents = eventsAt(Level.WARN);
		assertThat(warnEvents).hasSize(1);
		assertThat(warnEvents.get(0).getFormattedMessage()).contains("수집 실패 1/4 단위");
	}

	@Test
	@DisplayName("모든 단위가 실패하면 아이템은 0건이고 실패 수가 전체 수와 같다 - 예전에는 이 상태가 조용한 COMPLETED 였다")
	void allFetchFailure_recordsFailedEqualsTotal() {
		when(tourRegionRepository.findDistinctAreaCode()).thenReturn(List.of("1", "2"));
		when(tourApiClient.fetchAllPagesForArea(1, 12, NUM_OF_ROWS, SERVICE_KEY)).thenThrow(
			fetchFailure("areaCode: 1"));
		when(tourApiClient.fetchAllPagesForArea(2, 12, NUM_OF_ROWS, SERVICE_KEY)).thenThrow(
			fetchFailure("areaCode: 2"));

		TourApiItemReader reader = createReader(new int[] {12});
		ExecutionContext executionContext = new ExecutionContext();
		reader.open(executionContext);

		assertThat(executionContext.getInt(TourApiItemReader.FETCH_TOTAL_COUNT_KEY)).isEqualTo(2);
		assertThat(executionContext.getInt(TourApiItemReader.FETCH_FAILED_COUNT_KEY)).isEqualTo(2);
		assertThat(reader.read()).isNull();
		assertThat(eventsAt(Level.ERROR)).hasSize(2);
	}

	@Test
	@DisplayName("BusinessException 이 아닌 예외(코드 결함)는 잡지 않고 전파한다 - Step 이 FAILED 로 끝나야 한다")
	void nonBusinessException_propagates() {
		when(tourRegionRepository.findDistinctAreaCode()).thenReturn(List.of("1"));
		when(tourApiClient.fetchAllPagesForArea(1, 12, NUM_OF_ROWS, SERVICE_KEY))
			.thenThrow(new IllegalStateException("코드 결함 가정"));

		TourApiItemReader reader = createReader(new int[] {12});
		ExecutionContext executionContext = new ExecutionContext();

		assertThatThrownBy(() -> reader.open(executionContext))
			.isInstanceOf(IllegalStateException.class);
		// 건너뛰지 않았으므로 '수집 실패' 로그도, 집계도 남지 않는다
		assertThat(eventsAt(Level.ERROR)).isEmpty();
		assertThat(executionContext.containsKey(TourApiItemReader.FETCH_TOTAL_COUNT_KEY)).isFalse();
	}

	@Test
	@DisplayName("tour_region 의 areaCode 가 숫자가 아니면 수집 실패가 아니라 데이터 결함이므로 API 호출 없이 전파한다")
	void nonNumericAreaCode_propagatesWithoutCallingApi() {
		when(tourRegionRepository.findDistinctAreaCode()).thenReturn(List.of("abc"));

		TourApiItemReader reader = createReader(new int[] {12});

		assertThatThrownBy(() -> reader.open(new ExecutionContext()))
			.isInstanceOf(NumberFormatException.class);
		verifyNoInteractions(tourApiClient);
	}

	private TourApiItemReader createReader(int[] contentTypeIds) {
		return new TourApiItemReader(tourApiClient, tourRegionRepository, SERVICE_KEY, contentTypeIds, NUM_OF_ROWS);
	}

	// TourApiClient 가 실제로 던지는 형태 - I/O 실패를 cause 로 감싼 EXTERNAL_API_FAILED
	private BusinessException fetchFailure(String detail) {
		return new BusinessException(CommonErrorCode.EXTERNAL_API_FAILED, "TourAPI 관광정보 조회 실패. " + detail,
			new RuntimeException("Connection refused"));
	}

	private List<ILoggingEvent> eventsAt(Level level) {
		return logAppender.list.stream()
			.filter(event -> event.getLevel() == level)
			.toList();
	}
}
