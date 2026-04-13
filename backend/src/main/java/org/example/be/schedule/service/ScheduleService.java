package org.example.be.schedule.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.group.service.GroupService;
import org.example.be.member.entity.Member;
import org.example.be.member.service.MemberService;
import org.example.be.place.accommodation.entity.Accommodation;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.example.be.place.entity.PlaceType;
import org.example.be.place.region.TourRegionRepository;
import org.example.be.place.restaurant.entity.Restaurant;
import org.example.be.place.restaurant.repository.RestaurantRepository;
import org.example.be.place.theme.PlaceCategory;
import org.example.be.place.theme.PlaceCategoryRepository;
import org.example.be.place.touristSpot.entity.TouristSpot;
import org.example.be.place.touristSpot.repository.TouristSpotRepository;
import org.example.be.schedule.dto.request.SchedulePlaceReqBody;
import org.example.be.schedule.dto.request.ScheduleReqBody;
import org.example.be.schedule.dto.response.ScheduleResBody;
import org.example.be.schedule.dto.response.ScheduleSummaryResBody;
import org.example.be.schedule.entity.Schedule;
import org.example.be.schedule.entity.SchedulePlace;
import org.example.be.schedule.repository.SchedulePlaceRepository;
import org.example.be.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;
	private final GroupRepository groupRepository;
	private final PlaceValidationService placeValidationService;
	private final GroupService groupService;
	private final SchedulePlaceRepository schedulePlaceRepository;
	private final TouristSpotRepository touristSpotRepository;
	private final AccommodationRepository accommodationRepository;
	private final RestaurantRepository restaurantRepository;
	private final TourRegionRepository tourRegionRepository;
	private final PlaceCategoryRepository placeCategoryRepository;
	private final MemberService memberService;

	// 일정 생성
	@Transactional
	public ScheduleResBody createSchedule(ScheduleReqBody reqBody, Long userId) {
		if (reqBody.startSchedule().isAfter(reqBody.endSchedule())) {
			throw new BusinessException(ErrorCode.SCHEDULE_INVALID_PERIOD,
				"일정 생성 실패 - 시작 날짜가 종료 날짜보다 이후일 수 없음 startSchedule: " + reqBody.startSchedule() + ", endSchedule: "
					+ reqBody.endSchedule());
		}

		Group group = groupRepository.findByGroupName(reqBody.groupName())
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND,
				"일정 생성 실패 - 그룹 찾을 수 없음 groupName: " + reqBody.groupName()));

		// 그룹 창설자인지 검증
		groupService.validateGroupCreator(group.getGroupName(), userId);

		// 이미 일정이 존재하는지 검사
		scheduleRepository.findByGroup(group).ifPresent(existingSchedule -> {
			throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_EXIST,
				"일정 생성 실패 - 그룹에 이미 일정 존재 groupName: " + reqBody.groupName());
		});

		Schedule schedule = Schedule.create(reqBody.startSchedule(), reqBody.endSchedule(), group);

		for (SchedulePlaceReqBody places : reqBody.schedulePlaces()) {
			placeValidationService.validateContentIdByPlaceType(places.placeType(), places.contentId());

			SchedulePlace.create(schedule,
				places.contentId(),
				places.placeType(),
				places.visitStart(),
				places.visitedEnd(),
				places.dayOrder(),
				places.orderInDay()
			);
		}

		try {
			Schedule savedSchedule = scheduleRepository.save(schedule);
			return ScheduleResBody.from(savedSchedule);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_CREATION_FAILED, "일정 생성 실패 - message: " + e.getMessage());
		}
	}

	// 일정 조회
	@Transactional(readOnly = true)
	public ScheduleResBody getScheduleByGroupName(String groupName) {
		Group group = groupRepository.findByGroupName(groupName)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND, "groupName: " + groupName));

		Schedule schedule = scheduleRepository.findByGroup(group)
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "groupName: " + groupName));

		return ScheduleResBody.from(schedule);
	}

	// 일정 요약 목록 조회
	@Transactional(readOnly = true)
	public List<ScheduleSummaryResBody> getScheduleList(Long userId) {
		Member user = memberService.getById(userId);

		List<Group> groups = groupRepository.findByMembersContaining(user);
		if (groups.isEmpty()) {
			return Collections.emptyList();
		}

		List<Schedule> schedules = scheduleRepository.findAllByGroupsFetchJoin(groups);

		// 1. 모든 SchedulePlace에서 고유한 contentId 수집 및 PlaceType별로 그룹화
		Map<PlaceType, Set<String>> contentIdsByType = schedules.stream()
			.flatMap(s -> s.getSchedulePlaces().stream())
			.collect(Collectors.groupingBy(
				SchedulePlace::getPlaceType,
				Collectors.mapping(SchedulePlace::getContentId, Collectors.toSet())
			));

		// 2. 타입별 장소 엔티티들을 한 번에 조회하여 Map에 저장
		Map<String, TouristSpot> touristSpotMap = touristSpotRepository.findAllByContentIdIn(
				new ArrayList<>(contentIdsByType.getOrDefault(PlaceType.TouristSpot, Collections.emptySet())))
			.stream()
			.collect(Collectors.toMap(TouristSpot::getContentId, Function.identity()));

		Map<String, Restaurant> restaurantMap = restaurantRepository.findAllByContentIdIn(
				new ArrayList<>(contentIdsByType.getOrDefault(PlaceType.Restaurant, Collections.emptySet())))
			.stream()
			.collect(Collectors.toMap(Restaurant::getContentId, Function.identity()));

		Map<String, Accommodation> accommodationMap = accommodationRepository.findAllByContentIdIn(
				new ArrayList<>(contentIdsByType.getOrDefault(PlaceType.Accommodation, Collections.emptySet())))
			.stream()
			.collect(Collectors.toMap(Accommodation::getContentId, Function.identity()));

		// 3. TourRegion 조회에 필요한 areaCode 및 siGunGuCode 수집
		Set<String> regionCodeKeys = new HashSet<>();
		touristSpotMap.values().forEach(ts -> regionCodeKeys.add(ts.getAreaCode() + "-" + ts.getSiGunGuCode()));
		restaurantMap.values().forEach(r -> regionCodeKeys.add(r.getAreaCode() + "-" + r.getSiGunGuCode()));
		accommodationMap.values().forEach(a -> regionCodeKeys.add(a.getAreaCode() + "-" + a.getSiGunGuCode()));

		// 4. TourRegion 한 번에 조회하여 Map에 저장 (findAll 제거 -> 필요한 지역만 조회)
		Map<String, String> tourRegionMap = new HashMap<>();
		for (String key : regionCodeKeys) {
			String[] parts = key.split("-");
			tourRegionRepository.findByAreaCodeAndSiGunGuCode(parts[0], parts[1])
				.ifPresent(tr -> tourRegionMap.put(key, tr.getRegion()));
		}

		// 5. PlaceCategory 조회에 필요한 cat3 수집
		Set<String> cat3Codes = touristSpotMap.values().stream()
			.map(TouristSpot::getCat3)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		// 6. PlaceCategory 한 번에 조회하여 Map에 저장
		Map<String, String> placeCategoryMap = placeCategoryRepository.findAllByCat3In(new ArrayList<>(cat3Codes))
			.stream()
			.collect(Collectors.toMap(PlaceCategory::getCat3, PlaceCategory::getTheme));

		Map<Long, Schedule> scheduleMap = schedules.stream()
			.collect(Collectors.toMap(s -> s.getGroup().getId(), Function.identity()));

		return groups.stream()
			.map(group -> {
				Schedule schedule = scheduleMap.get(group.getId());

				if (schedule == null) {
					return ScheduleSummaryResBody.empty(group.getGroupName());
				}

				List<SchedulePlace> places = schedule.getSchedulePlaces();

				SchedulePlace firstAnyPlace = places.stream()
					.min(Comparator.comparing(SchedulePlace::getVisitStart))
					.orElse(null);

				SchedulePlace firstTouristSpotPlace = places.stream()
					.filter(p -> p.getPlaceType() == PlaceType.TouristSpot)
					.min(Comparator.comparing(SchedulePlace::getVisitStart))
					.orElse(null);

				// 핵심: 미리 로드된 맵들을 헬퍼 메서드에 넘겨서 정보 추출
				String region = getRegionFromPlace(
					firstAnyPlace, touristSpotMap, restaurantMap, accommodationMap, tourRegionMap
				);
				String theme = getThemeFromPlace(
					firstTouristSpotPlace, touristSpotMap, placeCategoryMap
				);

				return ScheduleSummaryResBody.from(
					group.getGroupName(),
					region,
					theme,
					schedule.getStartSchedule(),
					schedule.getEndSchedule()
				);

			})
			.toList();
	}

	// 일정 수정
	@Transactional
	public ScheduleResBody updateSchedule(Long scheduleId, ScheduleReqBody reqBody, Long userId) {
		if (reqBody.startSchedule().isAfter(reqBody.endSchedule())) {
			throw new BusinessException(ErrorCode.SCHEDULE_INVALID_PERIOD);
		}

		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "scheduleId: " + scheduleId));

		Group group = groupRepository.findByGroupName(reqBody.groupName())
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND, "groupName: " + reqBody.groupName()));

		// 그룹 창설자 검증
		groupService.validateGroupCreator(group.getGroupName(), userId);

		schedule.update(reqBody.startSchedule(), reqBody.endSchedule(), group);

		schedule.getSchedulePlaces().clear();
		scheduleRepository.flush();

		for (SchedulePlaceReqBody places : reqBody.schedulePlaces()) {
			placeValidationService.validateContentIdByPlaceType(places.placeType(), places.contentId());
			SchedulePlace.create(schedule,
				places.contentId(),
				places.placeType(),
				places.visitStart(),
				places.visitedEnd(),
				places.dayOrder(),
				places.orderInDay()
			);
		}
		try {
			Schedule updatedSchedule = scheduleRepository.save(schedule);
			return ScheduleResBody.from(updatedSchedule);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_UPDATE_FAILED, "일정 수정 실패 - message: " + e.getMessage());
		}
	}

	// 일정 삭제
	@Transactional
	public void deleteSchedule(Long scheduleId, Long userId) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "scheduleId: " + scheduleId));

		groupService.validateGroupCreator(schedule.getGroup().getGroupName(), userId);

		try {
			scheduleRepository.delete(schedule);
			scheduleRepository.flush(); // 즉시 DB 제약 조건 확인
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_DELETE_FAILED, "일정 삭제 실패 - message: " + e.getMessage());
		}
	}

	// --- N+1 해결을 위한 새로운 헬퍼 메서드들 ---
	private String getRegionFromPlace(
		SchedulePlace place,
		Map<String, TouristSpot> touristSpotMap,
		Map<String, Restaurant> restaurantMap,
		Map<String, Accommodation> accommodationMap,
		Map<String, String> tourRegionMap
	) {
		if (place == null) {
			return null;
		}

		String regionCodeKey = null;
		switch (place.getPlaceType()) {
			case TouristSpot:
				TouristSpot ts = touristSpotMap.get(place.getContentId());
				if (ts != null)
					regionCodeKey = ts.getAreaCode() + "-" + ts.getSiGunGuCode();
				break;
			case Restaurant:
				Restaurant r = restaurantMap.get(place.getContentId());
				if (r != null)
					regionCodeKey = r.getAreaCode() + "-" + r.getSiGunGuCode();
				break;
			case Accommodation:
				Accommodation a = accommodationMap.get(place.getContentId());
				if (a != null)
					regionCodeKey = a.getAreaCode() + "-" + a.getSiGunGuCode();
				break;
		}
		return regionCodeKey != null ? tourRegionMap.get(regionCodeKey) : null;
	}

	// SchedulePlace와 미리 로드된 맵들을 이용하여 테마 정보 추출
	private String getThemeFromPlace(
		SchedulePlace place,
		Map<String, TouristSpot> touristSpotMap,
		Map<String, String> placeCategoryMap
	) {
		if (place == null || place.getPlaceType() != PlaceType.TouristSpot) {
			return null;
		}

		TouristSpot ts = touristSpotMap.get(place.getContentId());
		if (ts == null || ts.getCat3() == null) {
			return null;
		}

		return placeCategoryMap.get(ts.getCat3());
	}
}