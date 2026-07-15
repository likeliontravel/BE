package org.example.be.domain.schedule.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.example.be.domain.group.entity.Group;
import org.example.be.domain.schedule.dto.response.NearestScheduleResBody;
import org.example.be.domain.schedule.entity.Schedule;
import org.example.be.domain.schedule.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ScheduleService 가장 가까운 예정 일정 조회 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ScheduleServiceNearestScheduleTest {

	private static final Long USER_ID = 10L;
	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final LocalDate TODAY = LocalDate.of(2026, 7, 16);

	@Mock
	private ScheduleRepository scheduleRepository;
	@Mock
	private Clock clock;

	@InjectMocks
	private ScheduleService scheduleService;

	@BeforeEach
	void setUpClock() {
		when(clock.instant()).thenReturn(Instant.parse("2026-07-16T05:00:00Z"));
		when(clock.getZone()).thenReturn(SEOUL);
	}

	@Test
	@DisplayName("오늘 시작 시각이 이미 지났어도 날짜 기준 D-Day는 0이다")
	void todaySchedule_returnsZeroDDay() {
		Schedule schedule = schedule("제주 여행", LocalDateTime.of(2026, 7, 16, 8, 0));
		when(scheduleRepository.findNearestUpcomingByMemberId(USER_ID, TODAY.atStartOfDay()))
			.thenReturn(Optional.of(schedule));

		Optional<NearestScheduleResBody> result = scheduleService.getNearestSchedule(USER_ID);

		assertThat(result).hasValueSatisfying(response -> {
			assertThat(response.groupName()).isEqualTo("제주 여행");
			assertThat(response.startSchedule()).isEqualTo(TODAY);
			assertThat(response.dDay()).isZero();
		});
		verify(scheduleRepository).findNearestUpcomingByMemberId(USER_ID, TODAY.atStartOfDay());
	}

	@Test
	@DisplayName("미래 일정은 오늘과 시작 날짜 사이의 날짜 차이를 반환한다")
	void futureSchedule_returnsDateDifference() {
		Schedule schedule = schedule("부산 여행", LocalDateTime.of(2026, 7, 19, 23, 30));
		when(scheduleRepository.findNearestUpcomingByMemberId(USER_ID, TODAY.atStartOfDay()))
			.thenReturn(Optional.of(schedule));

		Optional<NearestScheduleResBody> result = scheduleService.getNearestSchedule(USER_ID);

		assertThat(result).hasValueSatisfying(response -> {
			assertThat(response.startSchedule()).isEqualTo(LocalDate.of(2026, 7, 19));
			assertThat(response.dDay()).isEqualTo(3L);
		});
	}

	@Test
	@DisplayName("시작될 일정이 없으면 빈 Optional을 반환한다")
	void noUpcomingSchedule_returnsEmpty() {
		when(scheduleRepository.findNearestUpcomingByMemberId(USER_ID, TODAY.atStartOfDay()))
			.thenReturn(Optional.empty());

		Optional<NearestScheduleResBody> result = scheduleService.getNearestSchedule(USER_ID);

		assertThat(result).isEmpty();
	}

	private Schedule schedule(String groupName, LocalDateTime startSchedule) {
		Group group = mock(Group.class);
		when(group.getGroupName()).thenReturn(groupName);
		return Schedule.create(startSchedule, startSchedule.plusDays(1), group);
	}
}
