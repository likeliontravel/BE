package org.example.be.external.tourapi.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.example.be.domain.place.accommodation.dto.AccommodationResBody;
import org.example.be.domain.place.restaurant.dto.RestaurantResBody;
import org.example.be.domain.place.touristspot.dto.TouristSpotResBody;
import org.example.be.external.tourapi.service.AccommodationFetchService;
import org.example.be.external.tourapi.service.RestaurantFetchService;
import org.example.be.external.tourapi.service.TouristSpotFetchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Tourism fetch 엔드포인트 응답 규격 통합 테스트 (Task 3-3 · D4).
// /tourism/fetch/{touristSpot,accommodation,restaurant}/{areaCode} 3개만 CommonResponse 없이 배열을 그대로 반환했다.
// 같은 컨트롤러의 /fetch/touristSpot/all 은 이미 래핑돼 있어, 규격 밖 응답이 다시 생기지 않도록 루트 shape 을 고정한다.
// 서비스만 mock 으로 갈아끼운다 — 실제 TourAPI 호출과 로컬 DB insert 를 피하기 위함이다. (Task 3-3 사용자 결정 D1)
// AreaCodeResolver 는 정적 Map 이라 실빈을 그대로 쓴다.
@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
@DisplayName("Tourism fetch 엔드포인트 CommonResponse 규격 통합 테스트")
class TourismControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TouristSpotFetchService touristSpotFetchService;

	@MockitoBean
	private AccommodationFetchService accommodationFetchService;

	@MockitoBean
	private RestaurantFetchService restaurantFetchService;

	// 서울 — AreaCodeResolver 에 존재하는 지역코드
	private static final int SEOUL_AREA_CODE = 1;

	// AreaCodeResolver 에 없는 지역코드
	private static final int UNKNOWN_AREA_CODE = 99;

	@Test
	@DisplayName("GET /tourism/fetch/touristSpot/{areaCode} — CommonResponse 로 래핑된다")
	void fetchTouristSpots_wrapsInCommonResponse() throws Exception {
		TouristSpotResBody spot = TouristSpotResBody.builder().contentId("126508").title("경복궁").build();
		when(touristSpotFetchService.getTouristSpots(anyInt(), anyString(), anyInt(), anyInt(), anyInt()))
			.thenReturn(List.of(spot));

		mockMvc.perform(get("/tourism/fetch/touristSpot/{areaCode}", SEOUL_AREA_CODE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message").value("관광지 정보 저장 성공"))
			.andExpect(jsonPath("$.data", hasSize(1)))
			.andExpect(jsonPath("$.data[0].contentId").value("126508"));
	}

	@Test
	@DisplayName("GET /tourism/fetch/accommodation/{areaCode} — CommonResponse 로 래핑된다")
	void fetchAccommodations_wrapsInCommonResponse() throws Exception {
		AccommodationResBody accommodation = AccommodationResBody.builder().contentId("142785").title("테스트 호텔").build();
		when(accommodationFetchService.getAccommodations(anyInt(), anyString(), anyInt(), anyInt()))
			.thenReturn(List.of(accommodation));

		mockMvc.perform(get("/tourism/fetch/accommodation/{areaCode}", SEOUL_AREA_CODE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message").value("숙소 정보 저장 성공"))
			.andExpect(jsonPath("$.data", hasSize(1)))
			.andExpect(jsonPath("$.data[0].contentId").value("142785"));
	}

	@Test
	@DisplayName("GET /tourism/fetch/restaurant/{areaCode} — CommonResponse 로 래핑된다")
	void fetchRestaurants_wrapsInCommonResponse() throws Exception {
		RestaurantResBody restaurant = RestaurantResBody.builder().contentId("2869760").title("테스트 식당").build();
		when(restaurantFetchService.getData(anyInt(), anyInt(), anyInt(), anyInt()))
			.thenReturn(List.of(restaurant));

		mockMvc.perform(get("/tourism/fetch/restaurant/{areaCode}", SEOUL_AREA_CODE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message").value("식당 정보 저장 성공"))
			.andExpect(jsonPath("$.data", hasSize(1)))
			.andExpect(jsonPath("$.data[0].contentId").value("2869760"));
	}

	@Test
	@DisplayName("없는 지역코드 — 서비스를 호출하지 않고 400 INVALID_REGION (래핑 전후 무변화)")
	void unknownAreaCode_returns400InvalidRegion() throws Exception {
		mockMvc.perform(get("/tourism/fetch/touristSpot/{areaCode}", UNKNOWN_AREA_CODE))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value("INVALID_REGION"));

		verifyNoInteractions(touristSpotFetchService);
	}
}
