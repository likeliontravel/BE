package org.example.be.domain.schedule.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.example.be.domain.schedule.service.SchedulePlaceService;
import org.example.be.global.security.config.SecurityUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

// SchedulePlaceController 요청 경계 통합 테스트.
// @SpringBootTest 로 전체 컨텍스트(실제 보안 필터·바인딩·GlobalExceptionHandler)를 띄우되,
// SchedulePlaceService 만 mock 으로 갈아끼워 "래퍼 바디 검증 → 400 / 전체삭제 위임"이라는 컨트롤러 경계만 확인한다.
// (검증 실패는 서비스 도달 전 바인딩 단계라 mock 으로 충분 — 실제 DB 데이터는 건드리지 않는다.)
@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
@DisplayName("SchedulePlaceController 요청 경계 통합 테스트")
class SchedulePlaceControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SchedulePlaceService schedulePlaceService;

	private static final long USER_ID = 10L;

	// 인증된 사용자(SecurityUser principal)를 SecurityContext 에 주입. (CSRF 는 disable 이라 불필요)
	private static RequestPostProcessor authedUser() {
		SecurityUser principal = new SecurityUser(USER_ID, "tester@example.com", "pw", "tester", List.of());
		return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
	}

	@Test
	@DisplayName("PUT 빈 배열이면 @NotEmpty 위반으로 400 (서비스 미호출)")
	void put_emptyList_returns400() throws Exception {
		String body = """
			{"schedulePlaces":[]}
			""";

		mockMvc.perform(put("/schedule/detail/1")
				.with(authedUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(schedulePlaceService);
	}

	@Test
	@DisplayName("PUT 원소 contentId 가 공백이면 cascade 검증으로 400 (서비스 미호출)")
	void put_blankContentIdElement_returns400() throws Exception {
		String body = """
			{"schedulePlaces":[
			  {"schedulePlaceId":1,"contentId":" ","placeType":"TOURISTSPOT",
			   "visitStart":"2026-06-06T10:00:00","visitedEnd":"2026-06-06T11:00:00","dayOrder":1,"orderInDay":1}
			]}
			""";

		mockMvc.perform(put("/schedule/detail/1")
				.with(authedUser())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(schedulePlaceService);
	}

	@Test
	@DisplayName("DELETE /detail/{scheduleId} 는 서비스 deleteAllSchedulePlaces 로 위임하고 200")
	void delete_delegatesToService() throws Exception {
		when(schedulePlaceService.deleteAllSchedulePlaces(eq(1L), eq(USER_ID))).thenReturn(List.of());

		mockMvc.perform(delete("/schedule/detail/1")
				.with(authedUser()))
			.andExpect(status().isOk());

		verify(schedulePlaceService).deleteAllSchedulePlaces(1L, USER_ID);
	}
}