package org.example.be.schedule.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.group.entitiy.Group;
import org.example.be.group.repository.GroupRepository;
import org.example.be.group.service.GroupService;
import org.example.be.member.entity.Member;
import org.example.be.member.service.MemberService;
import org.example.be.place.accommodation.repository.AccommodationRepository;
import org.example.be.place.entity.PlaceType;
import org.example.be.place.region.TourRegion;
import org.example.be.place.region.TourRegionRepository;
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

		// 사용자의 모든 그룹에 대한 일정을 한 번에 Fetch Join으로 가져옴 (N+1 방지)
		List<Schedule> schedules = scheduleRepository.findAllByGroupsFetchJoin(groups);

		Map<Long, Schedule> scheduleMap = schedules.stream()
			.collect(Collectors.toMap(s -> s.getGroup().getId(), Function.identity()));

		return groups.stream()
			.map(group -> {
				Schedule schedule = scheduleMap.get(group.getId());

				if (schedule == null) {
					return ScheduleSummaryResBody.empty(group.getGroupName());
				}

				List<SchedulePlace> places = schedule.getSchedulePlaces();

				// 1) region: 타입 무관, 가장 이른 visitStart
				SchedulePlace firstAnyPlace = places.stream()
					.min(Comparator.comparing(SchedulePlace::getVisitStart))
					.orElse(null);

				// 2) theme: TouristSpot 중 가장 이른 visitStart
				SchedulePlace firstTouristSpotPlace = places.stream()
					.filter(p -> p.getPlaceType() == PlaceType.TouristSpot)
					.min(Comparator.comparing(SchedulePlace::getVisitStart))
					.orElse(null);

				return ScheduleSummaryResBody.from(
					group.getGroupName(),
					resolveRegion(firstAnyPlace),
					resolveThemeFromTouristSpot(firstTouristSpotPlace),
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
			scheduleRepository.flush();
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_DELETE_FAILED, "일정 삭제 실패 - message: " + e.getMessage());
		}
	}

	private String resolveRegion(SchedulePlace place) {
		if (place == null) {
			return null;
		}

		switch (place.getPlaceType()) {
			case TouristSpot: {
				TouristSpot ts = touristSpotRepository.findByContentId(place.getContentId()).orElse(null);
				if (ts == null)
					return null;
				return tourRegionRepository
					.findByAreaCodeAndSiGunGuCode(ts.getAreaCode(), ts.getSiGunGuCode())
					.map(TourRegion::getRegion)
					.orElse(null);
			}
			case Restaurant: {
				org.example.be.place.restaurant.entity.Restaurant r =
					restaurantRepository.findByContentId(place.getContentId()).orElse(null);
				if (r == null)
					return null;
				return tourRegionRepository
					.findByAreaCodeAndSiGunGuCode(r.getAreaCode(), r.getSiGunGuCode())
					.map(TourRegion::getRegion)
					.orElse(null);
			}
			case Accommodation: {
				org.example.be.place.accommodation.entity.Accommodation a =
					accommodationRepository.findByContentId(place.getContentId()).orElse(null);
				if (a == null)
					return null;
				return tourRegionRepository
					.findByAreaCodeAndSiGunGuCode(a.getAreaCode(), a.getSiGunGuCode())
					.map(TourRegion::getRegion)
					.orElse(null);
			}
			default:
				return null;
		}
	}

	private String resolveThemeFromTouristSpot(SchedulePlace touristSpotPlace) {
		if (touristSpotPlace == null) {
			return "기타";
		}
		TouristSpot ts = touristSpotRepository.findByContentId(touristSpotPlace.getContentId()).orElse(null);
		if (ts == null) {
			return "기타";
		}
		return placeCategoryRepository.findByCat3(ts.getCat3())
			.map(PlaceCategory::getTheme)
			.orElse("기타");
	}

}
