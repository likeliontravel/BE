package org.example.be.domain.schedule.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.example.be.domain.group.service.GroupService;
import org.example.be.domain.schedule.dto.request.SchedulePlaceReqBody;
import org.example.be.domain.schedule.dto.request.SchedulePlaceUpdateReqBody;
import org.example.be.domain.schedule.dto.response.PlaceSimpleResBody;
import org.example.be.domain.schedule.dto.response.SchedulePlaceDeleteResBody;
import org.example.be.domain.schedule.dto.response.SchedulePlaceResBody;
import org.example.be.domain.schedule.entity.Schedule;
import org.example.be.domain.schedule.entity.SchedulePlace;
import org.example.be.domain.schedule.repository.SchedulePlaceRepository;
import org.example.be.domain.schedule.repository.ScheduleRepository;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulePlaceService {

	private final SchedulePlaceRepository schedulePlaceRepository;
	private final ScheduleRepository scheduleRepository;
	private final PlaceValidationService placeValidationService;
	private final GroupService groupService;

	// 일정 블록 생성 메서드 - 최초 "일정 저장하기" 시 처리 로직
	@Transactional
	public List<SchedulePlaceResBody> createSchedulePlaces(Long scheduleId, List<SchedulePlaceReqBody> reqBodies,
		Long userId) {
		var schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "scheduleId: " + scheduleId));

		// 권한 검증: 그룹 창설자만 세부 일정을 추가할 수 있음
		groupService.validateGroupCreator(schedule.getGroup().getGroupName(), userId);

		// 추가할 것이 없으면 조기 반환 (불필요한 검증 / 쿼리 스킵)
		if (reqBodies.isEmpty()) {
			return List.of();
		}

		// 요청 내 순서 슬롯(dayOrder, orderInDay) 중복, 장소 실재 검증
		validateNoDuplicateSlots(reqBodies.stream()
			.map(reqBody -> new SlotKey(reqBody.dayOrder(), reqBody.orderInDay()))
			.toList());
		placeValidationService.validateContentIdsExist(reqBodies.stream()
			.collect(Collectors.groupingBy(SchedulePlaceReqBody::placeType,
				Collectors.mapping(SchedulePlaceReqBody::contentId, Collectors.toSet()))));

		// 엔티티 생성 (create() 내부에서 schedule 컬렉션에 연결됨)
		List<SchedulePlace> places = reqBodies.stream()
			.map(reqBody -> SchedulePlace.create(
				schedule,
				reqBody.contentId(),
				reqBody.placeType(),
				reqBody.visitStart(),
				reqBody.visitedEnd(),
				reqBody.dayOrder(),
				reqBody.orderInDay()))
			.toList();

		List<SchedulePlace> savedPlaces = schedulePlaceRepository.saveAll(places);

		return toResBodies(savedPlaces);
	}

	// 특정 일정의 세부 장소 전체 블록들의 수정 후 최종 상태를 한 번에 교체한다. (추가/수정/삭제/순서변경 모두 일괄 반영)
	// 아래 설명에서 id는 schedulePlaceId를 말함.
	// 처리 방식 : id 키 in-place 머지 (기존 있던 행은 변경된 정보만 교체, 신규는 id 부여하며 추가, 삭제는 해당 행 삭제)
	// - 요청에 id 있고 기존에도 있음 -> upsert (PK, created_at 보존)
	// - 기존에만 있고 요청에 없음 -> Delete (orphanRemoval이 수행)
	// - 요청에 id 없음 -> Insert (새 PK 발급)
	// 주의: 컬렉션을 clear() 하지 않는다는 마인드로 설계됨 ( 기존 블록들 전부 제거하고 입력된 블록들 정보로 전부 Insert하지 않는다는 의미 )
	@Transactional
	public List<SchedulePlaceResBody> updateSchedulePlaces(Long scheduleId, List<SchedulePlaceUpdateReqBody> reqBodies,
		Long userId) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "scheduleId: " + scheduleId));

		// === 검증 시작 ===

		// 권한 검증: 그룹 창설자만 세부 일정을 수정할 수 있음
		groupService.validateGroupCreator(schedule.getGroup().getGroupName(), userId);

		// 현재 일정에 속한 기존 블록을 id로 찾을 수 있게 Map으로 색인 (아래에서 검증, 갱신 메서드가 공유)
		Map<Long, SchedulePlace> existingById = schedule.getSchedulePlaces().stream()
			.collect(Collectors.toMap(SchedulePlace::getId, Function.identity()));

		// 변경 전, 요청 전체를 검증하고 요청에 실린 기존 블록 id 집합을 받아온다.
		Set<Long> incomingIds = validateMergeRequest(reqBodies, existingById);

		// === 검증 끝, upsert 시작 ===
		// id 키 in-place merge
		// 1. DELETE: 요청에 없는 기존 블록 제거 -> orphanRemoval이 실제 삭제 수행
		schedule.getSchedulePlaces().removeIf(place -> !incomingIds.contains(place.getId()));
		// 2. UPDATE (id 있는 경우) / INSERT (id 없는 경우)
		for (SchedulePlaceUpdateReqBody reqBody : reqBodies) {
			if (reqBody.schedulePlaceId() != null) {
				existingById.get(reqBody.schedulePlaceId()).update(
					reqBody.contentId(),
					reqBody.placeType(),
					reqBody.visitStart(),
					reqBody.visitedEnd(),
					reqBody.dayOrder(),
					reqBody.orderInDay());
			} else {
				SchedulePlace.create(
					schedule,
					reqBody.contentId(),
					reqBody.placeType(),
					reqBody.visitStart(),
					reqBody.visitedEnd(),
					reqBody.dayOrder(),
					reqBody.orderInDay());
			}
		}

		// 보류된 INSERT/DELETE/UPDATE를 DB로 내보내 신규 블록의 PK를 확정시킨다.
		schedulePlaceRepository.flush();

		return toResBodies(schedule.getSchedulePlaces());
	}

	// 특정 일정(scheduleId)의 세부 장소 전체 삭제. (전용 DELETE /schedule/detail/{scheduleId}
	// 전부 삭제가 목적이므로 컬렉션을 clear() -> orphanRemoval이 실제 DELETE 수행
	@Transactional
	public List<SchedulePlaceDeleteResBody> deleteAllSchedulePlaces(Long scheduleId, Long userId) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "scheduleId: " + scheduleId));

		// 권한 검증: 그룹 창설자만 세부 일정을 삭제할 수 있음
		groupService.validateGroupCreator(schedule.getGroup().getGroupName(), userId);

		// clear() 전에 스냅샷을 복사해놓기(대상 블록 정보 응답용)
		List<SchedulePlaceDeleteResBody> deleted = schedule.getSchedulePlaces().stream()
			.map(SchedulePlaceDeleteResBody::from)
			.toList();

		// 실제 DELETE는 @Transactional 타고 커밋 시점에 orphanRemoval이 DELETE쿼리 수행
		schedule.getSchedulePlaces().clear();

		return deleted;

	}

	// === 보조 내부 메서드 ===

	// 머지 요청 전체를 검증하고, 요청에 실린(=기존 블록을 가리키는) schedulePlaceId 집합을 반환한다.
	// existingById: 이 일정에 현재 속한 블록들의 id -> 엔티티 색인 Map
	private Set<Long> validateMergeRequest(List<SchedulePlaceUpdateReqBody> reqBodies,
		Map<Long, SchedulePlace> existingById) {
		// 1. 요청 내 (dayOrder, orderInDay) 슬롯 중복 금지
		validateNoDuplicateSlots(reqBodies.stream()
			.map(reqBody -> new SlotKey(reqBody.dayOrder(), reqBody.orderInDay()))
			.toList());

		// 2. 요청한 장소들이 모두 실재하는지 묶음 검증 (타입 별 IN절 조회 1회씩만)
		placeValidationService.validateContentIdsExist(reqBodies.stream()
			.collect(Collectors.groupingBy(SchedulePlaceUpdateReqBody::placeType,
				Collectors.mapping(SchedulePlaceUpdateReqBody::contentId, Collectors.toSet()))));

		// 3. 요청에 실린 schedulePlaceId 추출 (신규 블록은 null이라 제외)
		List<Long> incomingIdList = reqBodies.stream()
			.map(SchedulePlaceUpdateReqBody::schedulePlaceId)
			.filter(Objects::nonNull)
			.toList();
		Set<Long> incomingIds = new HashSet<>(incomingIdList);

		// 3-a. 요청 내 schedulePlaceId 중복 금지 (같은 기존 블록을 두 번 가리키면 한 입력이 조용히 사라짐)
		if (incomingIds.size() != incomingIdList.size()) {
			throw new BusinessException(ErrorCode.SCHEDULE_PLACE_DUPLICATE_ID, "schedulePlaceIds: " + incomingIdList);
		}
		// 3-b. 이 일정에 속하지 않거나 존재하지 않는 id 금지 (타 일정 id 탈취 방지)
		for (Long incomingId : incomingIds) {
			if (!existingById.containsKey(incomingId)) {
				throw new BusinessException(ErrorCode.SCHEDULE_PLACE_NOT_FOUND,
					"이 일정에 속하지 않거나 존재하지 않는 schedulePlaceId: " + incomingId);
			}
		}
		return incomingIds;
	}

	// SchedulePlace 목록 -> 응답 DTO 목록 변환. 장소 상세(제목, 썸네일, 주소)를 묶음 조회해 채운다.
	private List<SchedulePlaceResBody> toResBodies(List<SchedulePlace> places) {
		Map<String, PlaceSimpleResBody> placeDetails = placeValidationService.getPlaceSimpleDetails(places);
		return places.stream()
			.map(place -> SchedulePlaceResBody.from(place, placeDetails.get(place.getContentId())))
			.toList();
	}

	// 요청 내 (dayOrder, orderInDay) 조합이 중복되지 않는지 검증
	private void validateNoDuplicateSlots(List<SlotKey> slots) {
		Set<SlotKey> uniqueSlots = new HashSet<>(slots);
		if (uniqueSlots.size() != slots.size()) {
			throw new BusinessException(ErrorCode.SCHEDULE_PLACE_DUPLICATE_ORDER,
				"중복된 (dayOrder, orderInDay) 슬롯이 있습니다.");
		}
	}

	// (dayOrder, orderInDay) 정렬 슬롯 키. record라 equals()/hashCode()가 자동 생성되어 Set 중복 검출에 사용된다.
	private record SlotKey(Integer dayOrder, Integer orderInDay) {
	}

}
