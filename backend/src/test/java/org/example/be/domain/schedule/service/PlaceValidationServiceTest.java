package org.example.be.domain.schedule.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.example.be.domain.place.accommodation.repository.AccommodationRepository;
import org.example.be.domain.place.restaurant.entity.Restaurant;
import org.example.be.domain.place.restaurant.repository.RestaurantRepository;
import org.example.be.domain.place.shared.type.PlaceType;
import org.example.be.domain.place.touristspot.entity.TouristSpot;
import org.example.be.domain.place.touristspot.repository.TouristSpotRepository;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// PlaceValidationService.validateContentIdsExist(묶음 존재 검증, N+1 해소) 단위 테스트
// 장소 reposiry 3종을 mock으로 끊고, 타입 별 IN절 조회 분기와 missing 검출만 검증한다.
@DisplayName("PlaceValidationService 묶음 장소 존재 검증 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PlaceValidationServiceTest {

	@Mock
	private TouristSpotRepository touristSpotRepository;
	@Mock
	private RestaurantRepository restaurantRepository;
	@Mock
	private AccommodationRepository accommodationRepository;

	@InjectMocks
	private PlaceValidationService placeValidationService;

	@Test
	@DisplayName("요청한 장소가 모두 존재하면 예외 없이 통과한다")
	void allExist_passes() {
		Map<PlaceType, Set<String>> request = Map.of(PlaceType.TOURISTSPOT, Set.of("C1", "C2"));

		TouristSpot c1 = touristSpot("C1");
		TouristSpot c2 = touristSpot("C2");
		when(touristSpotRepository.findAllByContentIdIn(anyList()))
			.thenReturn(List.of(c1, c2));

		assertThatCode(() -> placeValidationService.validateContentIdsExist(request))
			.doesNotThrowAnyException();
		verify(touristSpotRepository).findAllByContentIdIn(anyList());    // 타입 별 IN절 1회
	}

	@Test
	@DisplayName("존재하지 않는 contentId가 섞이면 PLACE_NOT_FOUND")
	void someMissing_throws() {
		Map<PlaceType, Set<String>> request = Map.of(PlaceType.TOURISTSPOT, Set.of("C1", "MISSING"));
		TouristSpot c1 = touristSpot("C1");
		when(touristSpotRepository.findAllByContentIdIn(anyList()))
			.thenReturn(List.of(c1));    // MISSING은 조회 결과에서 빠짐

		assertThatThrownBy(() -> placeValidationService.validateContentIdsExist(request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.PLACE_NOT_FOUND);
	}

	@Test
	@DisplayName("특정 타입의 contentId 집합이 비어 있으면 그 타입은 조회를 건너뛴다")
	void emptySet_skipsQuery() {
		Map<PlaceType, Set<String>> request = Map.of(PlaceType.TOURISTSPOT, Set.of());

		// 예외 없이 통과 확인
		assertThatCode(() -> placeValidationService.validateContentIdsExist(request))
			.doesNotThrowAnyException();
		// 쿼리 메서드가 호출되지 않았어야 함
		verify(touristSpotRepository, never()).findAllByContentIdIn(anyList());
	}

	@Test
	@DisplayName("여러 타입이 섞이면 타입별로 IN절 조회를 각각 1회씩만 수행한다 (N+1 해소)")
	void multipleTypes_queryOncePerType() {
		Map<PlaceType, Set<String>> request = Map.of(
			PlaceType.TOURISTSPOT, Set.of("C1"),
			PlaceType.RESTAURANT, Set.of("R1")
		);
		TouristSpot c1 = touristSpot("C1");
		Restaurant r1 = restaurant("R1");
		when(touristSpotRepository.findAllByContentIdIn(anyList())).thenReturn(List.of(c1));
		when(restaurantRepository.findAllByContentIdIn(anyList())).thenReturn(List.of(r1));

		assertThatCode(() -> placeValidationService.validateContentIdsExist(request))
			.doesNotThrowAnyException();
		verify(touristSpotRepository).findAllByContentIdIn(anyList());
		verify(restaurantRepository).findAllByContentIdIn(anyList());
		verify(accommodationRepository, never()).findAllByContentIdIn(
			anyList());    // accommodation 타입은 없었으므로 호출이 안되었어야 함

	}

	// === 테스트 헬퍼 메서드 ===

	// findExistingContentIds가 getContentId()만 사용하므로, 그 동작만 가진 가짜 엔티티로 대체
	private TouristSpot touristSpot(String contentId) {
		TouristSpot entity = mock(TouristSpot.class);
		when(entity.getContentId()).thenReturn(contentId);
		return entity;
	}

	private Restaurant restaurant(String contentId) {
		Restaurant entity = mock(Restaurant.class);
		when(entity.getContentId()).thenReturn(contentId);
		return entity;
	}
}
































