package org.example.be.external.tourapi.batch.listener;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.example.be.domain.place.touristspot.entity.TouristSpot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;

// PlaceSkipListener 단위 테스트.
// Spring Batch 는 skip 을 debug 로만 남기므로, 이 리스너가 WARN + 스택 + contentId 를 남기는지가 곧 "skip 이 운영에서 보이는가" 이다.
// 어느 예외가 skip 되고 몇 건에서 Step 이 멈추는지(skip 정책·상한)는 BatchConfig 설정의 몫이라 여기서 다루지 않는다.
@DisplayName("PlaceSkipListener skip 기록 단위 테스트")
class PlaceSkipListenerTest {

	private final PlaceSkipListener placeSkipListener = new PlaceSkipListener();

	// JUnit 은 테스트 메서드마다 인스턴스를 새로 만들므로, 이 appender 도 테스트마다 비어 있는 상태로 시작한다.
	private final ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
	private final Logger listenerLogger = (Logger)LoggerFactory.getLogger(PlaceSkipListener.class);

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
	@DisplayName("process 단계 skip 은 TourAPI 원본(Map)의 contentid 와 스택을 WARN 으로 남긴다")
	void skipInProcess_logsWarnWithContentIdAndStackTrace() {
		Map<String, Object> item = Map.of("contentid", "P-1");
		IllegalStateException cause = new IllegalStateException("TourRegion 매칭 실패 - areaCode: 99");

		placeSkipListener.onSkipInProcess(item, cause);

		ILoggingEvent event = singleEvent();
		assertThat(event.getLevel()).isEqualTo(Level.WARN);
		assertThat(event.getFormattedMessage()).isEqualTo("[TourDataSkip] process 단계 skip - contentId=P-1");
		assertThrowable(event, IllegalStateException.class, "TourRegion 매칭 실패 - areaCode: 99");
	}

	@Test
	@DisplayName("write 단계 skip 은 변환된 엔티티의 contentId 와 스택을 WARN 으로 남긴다")
	void skipInWrite_logsWarnWithEntityContentIdAndStackTrace() {
		TouristSpot touristSpot = TouristSpot.builder().contentId("W-1").build();
		DataIntegrityViolationException cause = new DataIntegrityViolationException("Data too long for column 'title'");

		placeSkipListener.onSkipInWrite(touristSpot, cause);

		ILoggingEvent event = singleEvent();
		assertThat(event.getLevel()).isEqualTo(Level.WARN);
		assertThat(event.getFormattedMessage()).isEqualTo("[TourDataSkip] write 단계 skip - contentId=W-1");
		assertThrowable(event, DataIntegrityViolationException.class, "Data too long for column 'title'");
	}

	@Test
	@DisplayName("read 단계 skip 도 스택과 함께 WARN 으로 남긴다")
	void skipInRead_logsWarnWithStackTrace() {
		IllegalStateException cause = new IllegalStateException("read 실패 가정");

		placeSkipListener.onSkipInRead(cause);

		ILoggingEvent event = singleEvent();
		assertThat(event.getLevel()).isEqualTo(Level.WARN);
		assertThat(event.getFormattedMessage()).isEqualTo("[TourDataSkip] read 단계 skip");
		assertThrowable(event, IllegalStateException.class, "read 실패 가정");
	}

	private ILoggingEvent singleEvent() {
		List<ILoggingEvent> events = logAppender.list;
		assertThat(events).hasSize(1);
		return events.get(0);
	}

	// 예외를 {} 자리에 넣으면 toString() 으로 메시지에 박혀 스택이 사라진다 - throwableProxy 가 있어야 스택이 남은 것이다.
	private void assertThrowable(ILoggingEvent event, Class<? extends Throwable> expectedType, String expectedMessage) {
		IThrowableProxy throwableProxy = event.getThrowableProxy();
		assertThat(throwableProxy).isNotNull();
		assertThat(throwableProxy.getClassName()).isEqualTo(expectedType.getName());
		assertThat(throwableProxy.getMessage()).isEqualTo(expectedMessage);
	}
}
