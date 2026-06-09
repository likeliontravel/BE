package org.example.be.domain.schedule.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.example.be.domain.group.entity.Group;
import org.example.be.domain.group.service.GroupService;
import org.example.be.domain.place.shared.type.PlaceType;
import org.example.be.domain.schedule.dto.request.SchedulePlaceUpdateReqBody;
import org.example.be.domain.schedule.dto.response.SchedulePlaceDeleteResBody;
import org.example.be.domain.schedule.dto.response.SchedulePlaceResBody;
import org.example.be.domain.schedule.entity.Schedule;
import org.example.be.domain.schedule.entity.SchedulePlace;
import org.example.be.domain.schedule.repository.SchedulePlaceRepository;
import org.example.be.domain.schedule.repository.ScheduleRepository;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

// SchedulePlaceService.updateSchedulePlaces(id 키 in-place 머지) 단위 테스트
// + deleteAllSchedulePlaces 단위테스트
// 협력자(repository / PlaceValidationService / GroupService)를 mock으로 끊고 머지 로직만 검증한다.
@DisplayName("SchedulePlaceService 일괄 수정(머지) 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SchedulePlaceServiceTest {

	@Mock
	private SchedulePlaceRepository schedulePlaceRepository;
	@Mock
	private ScheduleRepository scheduleRepository;
	@Mock
	private PlaceValidationService placeValidationService;
	@Mock
	private GroupService groupService;

	@InjectMocks
	private SchedulePlaceService schedulePlaceService;

	private static final Long SCHEDULE_ID = 1L;
	private static final Long USER_ID = 10L;
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 6, 10, 0);

	// === 정상 케이스 ===

	@Test
	@DisplayName("reorder: 순서만 바꿔도 schedulePlaceId(PK)와 엔티티 인스턴스가 보존된다")
	void reorder_preservesPkAndInstances() {
		Schedule schedule = newSchedule();
		addExisting(schedule, 1L, "C1", 1, 1);
		addExisting(schedule, 2L, "C2", 1, 2);
		addExisting(schedule, 3L, "C3", 1, 3);

		// 위에서 만든 일정 블록을 넘겨주도록 세팅
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
		// getPlaceSimpleDetails()호출 시 어떤 리스트가 담기든 빈 Map을 반환하도록 세팅
		when(placeValidationService.getPlaceSimpleDetails(anyList())).thenReturn(Collections.emptyMap());

		// 머지 전 인스턴스를 캡처해 두고, 이후 동일 인스턴스가 유지되는지 확인
		List<SchedulePlace> originals = new ArrayList<>(schedule.getSchedulePlaces());

		// 1~3 슬롯을 뒤집는 reorder(id는 변경 x)
		List<SchedulePlaceUpdateReqBody> reqBodies = List.of(
			req(1L, "C1", 1, 3),
			req(2L, "C2", 1, 1),
			req(3L, "C3", 1, 2)
		);

		List<SchedulePlaceResBody> result = schedulePlaceService.updateSchedulePlaces(SCHEDULE_ID, reqBodies, USER_ID);

		// PK 보존: 응답 id는 그대로 1,2,3
		assertThat(result).extracting(SchedulePlaceResBody::id).containsExactlyInAnyOrder(1L, 2L, 3L);
		// 인스턴스 보존: clear-reinsert가 아니라 제자리 갱신이므로 동일 객체가 유지되어야 함.
		assertThat(schedule.getSchedulePlaces()).containsExactlyInAnyOrderElementsOf(originals);
		// 슬롯은 바뀌어 있어야 함. (1 -> 3, 2 -> 1)
		assertThat(findById(schedule, 1L).getOrderInDay()).isEqualTo(3);
		assertThat(findById(schedule, 2L).getOrderInDay()).isEqualTo(1);
		verify(schedulePlaceRepository).flush();
	}

	@Test
	@DisplayName("혼합 머지: update / insert / delete가 한 번에 정확히 반영되어야 함")
	void mixedMerge_appliesAllSlots() {
		Schedule schedule = newSchedule();
		addExisting(schedule, 1L, "C1", 1, 1);
		SchedulePlace toDelete = addExisting(schedule, 2L, "C2", 1, 2);

		// 61~64라인과 동일
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
		when(placeValidationService.getPlaceSimpleDetails(anyList())).thenReturn(Collections.emptyMap());

		List<SchedulePlaceUpdateReqBody> reqBodies = List.of(
			req(1L, "C1", 1, 5),    // UPDATE (id 있음)
			req(null, "CNEW", 2, 1)    // INSERT (id 없음)
		);

		List<SchedulePlaceResBody> result = schedulePlaceService.updateSchedulePlaces(SCHEDULE_ID, reqBodies, USER_ID);

		// DELETE: id=2 블록은 orphanRemoval 대상으로 컬렉션에서 제거됨
		assertThat(schedule.getSchedulePlaces()).doesNotContain(toDelete);

		// 컬렉션은 update된 id=1 1건, insert된 id=null 1건 총 2건만 있어야 함.
		assertThat(schedule.getSchedulePlaces()).extracting(SchedulePlace::getId).containsExactlyInAnyOrder(1L, null);

		// UPDATE 반영 확인
		assertThat(findById(schedule, 1L).getOrderInDay()).isEqualTo(5);
		assertThat(result).hasSize(2);
		verify(schedulePlaceRepository).flush();
	}

	@Test
	@DisplayName("[특성] 빈 배열로 직접 호출 시 전체 삭제됨 — 단, 실제 API는 컨트롤러 @NotEmpty가 400으로 차단해 여기 도달 안 함")
	void emptyList_deletesAll() {
		// 주의: 이 동작은 updateSchedulePlaces '서비스 단독 호출' 시의 특성일 뿐이다.
		// PUT /detail/{scheduleId} 는 SchedulePlaceUpdateListReqBody 의 @NotEmpty 로 빈 배열을 400 거부하므로,
		// 정상 경로에서는 이 분기에 도달하지 않는다. 전체 삭제는 전용 deleteAllSchedulePlaces 가 담당한다.
		Schedule schedule = newSchedule();
		addExisting(schedule, 1L, "C1", 1, 1);
		addExisting(schedule, 2L, "C2", 1, 2);
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

		List<SchedulePlaceResBody> result = schedulePlaceService.updateSchedulePlaces(SCHEDULE_ID, List.of(), USER_ID);

		assertThat(schedule.getSchedulePlaces()).isEmpty();
		assertThat(result).isEmpty();
		verify(schedulePlaceRepository).flush();
	}

	@Test
	@DisplayName("전체 삭제: 모든 블록을 비우고, 삭제된 블록들의 경량 스냅샷을 반환한다")
	void deleteAll_clearsAndReturnsSnapshot() {
		Schedule schedule = newSchedule();
		addExisting(schedule, 1L, "C1", 1, 1);
		addExisting(schedule, 2L, "C2", 1, 2);
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

		List<SchedulePlaceDeleteResBody> result = schedulePlaceService.deleteAllSchedulePlaces(SCHEDULE_ID, USER_ID);

		// 반환: 삭제된 2건의 스냅샷 (schedulePlaceId·contentId 보존)
		assertThat(result).extracting(SchedulePlaceDeleteResBody::schedulePlaceId).containsExactlyInAnyOrder(1L, 2L);
		assertThat(result).extracting(SchedulePlaceDeleteResBody::contentId).containsExactlyInAnyOrder("C1", "C2");
		// 컬렉션은 비워짐 (orphanRemoval이 커밋 시점에 실제 DELETE 수행)
		assertThat(schedule.getSchedulePlaces()).isEmpty();
	}

	@Test
	@DisplayName("전체 삭제: 일정이 존재하지 않으면 SCHEDULE_NOT_FOUND 전파")
	void deleteAll_scheduleNotFound_throws() {
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> schedulePlaceService.deleteAllSchedulePlaces(SCHEDULE_ID, USER_ID))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
	}

	@Test
	@DisplayName("전체 삭제: 이미 빈 일정이면 빈 목록을 반환한다 (멱등)")
	void deleteAll_emptySchedule_returnsEmpty() {
		Schedule schedule = newSchedule();
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

		List<SchedulePlaceDeleteResBody> result = schedulePlaceService.deleteAllSchedulePlaces(SCHEDULE_ID, USER_ID);

		assertThat(result).isEmpty();
		assertThat(schedule.getSchedulePlaces()).isEmpty();
	}

	// === 검증 실패 케이스 ===

	@Test
	@DisplayName("일정이 존재하지 않으면 SCHEDULE_NOT_FOUND 전파되어야 함")
	void scheduleNotFound_throws() {
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> schedulePlaceService.updateSchedulePlaces(SCHEDULE_ID, List.of(), USER_ID))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
	}

	@Test
	@DisplayName("요청 내 (dayOrder, orderInDay) 슬롯이 중복되면 SCHEDULE_PLACE_DUPLICATE_ORDER 전파되어야 함")
	void duplicateSlot_throws() {
		Schedule schedule = newSchedule();
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

		// 신규 블록이 2건이 같은 슬롯 (1, 1)
		List<SchedulePlaceUpdateReqBody> reqBodies = List.of(
			req(null, "C1", 1, 1),
			req(null, "C2", 1, 1)
		);

		assertThatThrownBy(() -> schedulePlaceService.updateSchedulePlaces(SCHEDULE_ID, reqBodies, USER_ID))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.SCHEDULE_PLACE_DUPLICATE_ORDER);
	}

	@Test
	@DisplayName("요청에 동일한 schedulePlaceId가 중복되면 SCHEDULE_PLACE_DUPLICATE_ID 전파")
	void duplicateId_throws() {
		Schedule schedule = newSchedule();
		addExisting(schedule, 1L, "C1", 1, 1);
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

		// 같은 id=1을 두 번 가리킴(슬롯은 서로 달라 슬롯 검증은 통과 -> id 중복 검증까지 도달)
		List<SchedulePlaceUpdateReqBody> reqBodies = List.of(
			req(1L, "C1", 1, 1),
			req(1L, "C1", 1, 2)
		);

		assertThatThrownBy(() -> schedulePlaceService.updateSchedulePlaces(SCHEDULE_ID, reqBodies, USER_ID))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.SCHEDULE_PLACE_DUPLICATE_ID);
	}

	@Test
	@DisplayName("이 일정에 속하지 않는 id(타 일정 id 탈취)면 SCHEDULE_PLACE_NOT_FOUND 전파")
	void unknownId_throws() {
		Schedule schedule = newSchedule();
		addExisting(schedule, 1L, "C1", 1, 1);
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

		// 이 일정에 존재하지 않는 id=999
		List<SchedulePlaceUpdateReqBody> reqBodies = List.of(
			req(999L, "C1", 1, 1)
		);

		assertThatThrownBy(() -> schedulePlaceService.updateSchedulePlaces(SCHEDULE_ID, reqBodies, USER_ID))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.SCHEDULE_PLACE_NOT_FOUND);
	}

	@Test
	@DisplayName("이미 삭제되어 컬렉션에 없는 stale id면 SCHEDULE_PLACE_NOT_FOUND 전파")
	void staleId_throws() {
		Schedule schedule = newSchedule();
		addExisting(schedule, 1L, "C1", 1, 1);
		addExisting(schedule, 2L, "C2", 1, 2);
		when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

		// id=3은 과거에 있었으나 현재 컬렉션엔 없는(삭제된) 블록을 가리킴
		List<SchedulePlaceUpdateReqBody> reqBodies = List.of(
			req(3L, "C3", 2, 1)
		);

		assertThatThrownBy(() -> schedulePlaceService.updateSchedulePlaces(SCHEDULE_ID, reqBodies, USER_ID))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode").isEqualTo(ErrorCode.SCHEDULE_PLACE_NOT_FOUND);
	}

	// === 테스트 헬퍼 메서드 ===

	// 그룹은 머지 로직과 무관하므로 mock으로 둔다(getGroupName()은 null 반환, mock된 validateGroupCreator가 받음)
	private Schedule newSchedule() {
		Group group = mock(Group.class);
		return Schedule.create(NOW, NOW.plusDays(2), group);
	}

	// 기존 블록 생성: create()가 schedule 컬렉션에 연결하고, DB 영속화 없는 단위 테스트라 PK는 리플렉션으로 주입
	private SchedulePlace addExisting(Schedule schedule, long id, String contentId, int dayOrder, int orderInDay) {
		SchedulePlace place = SchedulePlace.create(schedule, contentId, PlaceType.TOURISTSPOT, NOW, NOW, dayOrder,
			orderInDay);
		ReflectionTestUtils.setField(place, "id", id);
		return place;
	}

	// 입력 데이터로 ReqBody를 만든다.
	private SchedulePlaceUpdateReqBody req(Long schedulePlaceId, String contentId, int dayOrder, int orderInDay) {
		return new SchedulePlaceUpdateReqBody(schedulePlaceId, contentId, PlaceType.TOURISTSPOT, NOW, NOW, dayOrder,
			orderInDay);
	}

	private SchedulePlace findById(Schedule schedule, long id) {
		return schedule.getSchedulePlaces().stream()
			.filter(place -> id == place.getId())
			.findFirst()
			.orElseThrow(() -> new AssertionError("id=" + id + " 블록을 찾을 수 없습니다."));
	}
}
