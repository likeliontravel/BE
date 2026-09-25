package org.example.be.external.tourapi.batch.processor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.example.be.domain.place.region.TourRegionRepository;
import org.example.be.domain.place.theme.PlaceCategoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

// PlaceProcessorHelper 의 타입 변환 헬퍼 단위 테스트 (C9).
// 변환 실패가 무음으로 null 이 되던 것을 WARN 으로 드러내되, TourAPI 가 '값 없음' 으로 보내는 빈 문자열은
// 실패가 아니므로 로그를 남기지 않는다 - 그 경계를 고정한다. (반환값은 이전과 동일해야 한다)
@DisplayName("PlaceProcessorHelper 타입 변환 단위 테스트")
class PlaceProcessorHelperTest {

	// 변환 헬퍼는 두 저장소를 쓰지 않지만 생성자가 요구하므로 mock 으로 채운다.
	private final PlaceProcessorHelper placeProcessorHelper =
		new PlaceProcessorHelper(mock(TourRegionRepository.class), mock(PlaceCategoryRepository.class));

	// JUnit 은 테스트 메서드마다 인스턴스를 새로 만들므로, 이 appender 도 테스트마다 비어 있는 상태로 시작한다.
	private final ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
	private final Logger helperLogger = (Logger)LoggerFactory.getLogger(PlaceProcessorHelper.class);

	@BeforeEach
	void attachLogAppender() {
		logAppender.start();
		helperLogger.addAppender(logAppender);
	}

	@AfterEach
	void detachLogAppender() {
		helperLogger.detachAppender(logAppender);
		logAppender.stop();
	}

	@Test
	@DisplayName("정상 값은 그대로 변환하고 로그를 남기지 않는다")
	void validValues_areConvertedWithoutLog() {
		assertThat(placeProcessorHelper.toDouble("127.5")).isEqualTo(127.5);
		assertThat(placeProcessorHelper.toInteger("6")).isEqualTo(6);
		// Jackson 이 숫자로 파싱해 Integer/Double 로 들어오는 경우도 문자열로 바꿔 처리한다
		assertThat(placeProcessorHelper.toDouble(37)).isEqualTo(37.0);
		assertThat(placeProcessorHelper.toInteger(6)).isEqualTo(6);

		assertThat(logAppender.list).isEmpty();
	}

	@Test
	@DisplayName("null 과 빈 문자열은 '값 없음' 이므로 로그 없이 null 을 돌려준다")
	void nullAndBlankValues_returnNullSilently() {
		assertThat(placeProcessorHelper.toDouble(null)).isNull();
		assertThat(placeProcessorHelper.toDouble("")).isNull();
		assertThat(placeProcessorHelper.toDouble("   ")).isNull();
		assertThat(placeProcessorHelper.toInteger(null)).isNull();
		assertThat(placeProcessorHelper.toInteger("")).isNull();
		assertThat(placeProcessorHelper.toInteger("   ")).isNull();

		// TourAPI 는 없는 값을 "" 로 보낸다. 여기서 로그를 남기면 정상 데이터가 로그를 뒤덮어 진짜 이상치가 묻힌다.
		assertThat(logAppender.list).isEmpty();
	}

	@Test
	@DisplayName("비어 있지 않은 값의 변환 실패는 null 을 돌려주되 WARN 으로 남긴다")
	void malformedValues_returnNullWithWarnLog() {
		assertThat(placeProcessorHelper.toDouble("abc")).isNull();
		assertThat(placeProcessorHelper.toInteger("6.0")).isNull();

		assertThat(logAppender.list).hasSize(2);
		assertThat(logAppender.list.get(0).getLevel()).isEqualTo(Level.WARN);
		assertThat(logAppender.list.get(0).getFormattedMessage())
			.isEqualTo("[TypeConvert] Double 변환 실패 - value=abc");
		assertThat(logAppender.list.get(1).getLevel()).isEqualTo(Level.WARN);
		assertThat(logAppender.list.get(1).getFormattedMessage())
			.isEqualTo("[TypeConvert] Integer 변환 실패 - value=6.0");
	}
}