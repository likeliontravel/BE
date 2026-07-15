package org.example.be.domain.schedule.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.example.be.domain.schedule.dto.response.NearestScheduleResBody;
import org.example.be.domain.schedule.service.ScheduleService;
import org.example.be.global.security.config.SecurityUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
@DisplayName("ScheduleController 가장 가까운 예정 일정 조회 통합 테스트")
class ScheduleControllerIT {

	private static final long USER_ID = 10L;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ScheduleService scheduleService;

	@Test
	@DisplayName("인증 사용자의 가장 가까운 일정을 CommonResponse로 반환한다")
	void nearestSchedule_returnsSuccessResponse() throws Exception {
		NearestScheduleResBody response = new NearestScheduleResBody(
			"제주 여행",
			LocalDate.of(2026, 7, 18),
			2L
		);
		when(scheduleService.getNearestSchedule(USER_ID)).thenReturn(Optional.of(response));

		mockMvc.perform(get("/schedule/nearest").with(authedUser()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message").value("가장 가까운 일정 조회 성공"))
			.andExpect(jsonPath("$.data.groupName").value("제주 여행"))
			.andExpect(jsonPath("$.data.startSchedule").value("2026-07-18"))
			.andExpect(jsonPath("$.data.dDay").value(2));

		verify(scheduleService).getNearestSchedule(USER_ID);
	}

	@Test
	@DisplayName("시작될 일정이 없으면 data 없이 200과 안내 메시지를 반환한다")
	void noUpcomingSchedule_returnsSuccessWithoutData() throws Exception {
		when(scheduleService.getNearestSchedule(USER_ID)).thenReturn(Optional.empty());

		mockMvc.perform(get("/schedule/nearest").with(authedUser()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message").value("시작될 일정이 없습니다."))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	@DisplayName("인증되지 않은 요청은 401이고 서비스에 도달하지 않는다")
	void unauthenticated_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/schedule/nearest"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.status").value(401))
			.andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."));

		verifyNoInteractions(scheduleService);
	}

	private static RequestPostProcessor authedUser() {
		SecurityUser principal = new SecurityUser(USER_ID, "tester@example.com", "pw", "tester", List.of());
		return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
	}
}
