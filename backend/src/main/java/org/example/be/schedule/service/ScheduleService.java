package org.example.be.schedule.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
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
		Group group = groupRepository.findByGroupName(reqBody.groupName())
			.orElseThrow(() -> new NoSuchElementException("해당 그룹을 찾을 수 없습니다."));

		// 그룹 창설자인지 검증
		groupService.validateGroupCreator(group.getGroupName(), userId);

		// 이미 일정이 존재하는지 검사
		scheduleRepository.findByGroup(group).ifPresent(existingSchedule -> {
			throw new IllegalStateException("해당 그룹에 이미 일정이 존재합니다.");
		});

		Schedule schedule = Schedule.create(reqBody.startSchedule(), reqBody.endSchedule(), group);

		for (SchedulePlaceReqBody places : reqBody.schedulePlaces()) {
			placeValidationService.validateContentIdByPlaceType(places.placeType(), places.contentId()); // 변경됨

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
			throw new ResourceCreationException("일정 생성에 실패했습니다.", e);
		}
	}

	// 일정 조회
	@Transactional(readOnly = true)
	public ScheduleResBody getScheduleByGroupName(String groupName) {
		Group group = groupRepository.findByGroupName(groupName)
			.orElseThrow(() -> new NoSuchElementException("해당 이름의 그룹이 존재하지 않습니다."));

		Schedule schedule = scheduleRepository.findByGroup(group)
			.orElseThrow(() -> new NoSuchElementException("해당 그룹에 일정이 존재하지 않습니다."));

		return ScheduleResBody.from(schedule);
	}

	// 일정 요약 목록 조회
	// 요청자의 가입된 그룹을 찾아 해당 그룹의 존재 일정 정보를 그룹별로 묶어 반환
	// 만약 그룹은 존재하나 일정이 없는 경우 scheduleFirstRegion값으로 "아직 일정이 생성되지 않았습니다" 전달 및 나머지값 null 반환
	@Transactional(readOnly = true)
	public List<ScheduleSummaryResBody> getScheduleList(Long userId) {
		Member user = memberService.getById(userId);

		List<Group> groups = groupRepository.findByMembersContaining(user);
		if (groups.isEmpty()) {
			return Collections.emptyList();
		}

		List<ScheduleSummaryResBody> scheduleSummaryList = new ArrayList<>();

		for (Group group : groups) {
			Optional<Schedule> scheduleOpt = scheduleRepository.findByGroup(group);

			// 스케줄이 없으면 안내문구만 반환
			if (scheduleOpt.isEmpty()) {
				scheduleSummaryList.add(
					ScheduleSummaryResBody.empty(group.getGroupName())
				);
				continue;
			}

			Schedule schedule = scheduleOpt.get();
			List<SchedulePlace> places = schedulePlaceRepository.findBySchedule(schedule);

			// 1) region: 타입 무관, 가장 이른 visitStart
			SchedulePlace firstAnyPlace = places.stream()
				.min(Comparator.comparing(SchedulePlace::getVisitStart))
				.orElse(null);
			String firstRegionName = resolveRegion(firstAnyPlace); // TouristSpot/Restaurant/Accommodation 모두 처리

			// 2) theme: TouristSpot 중 가장 이른 visitStart가 있을 때만, 없으면 "기타"
			SchedulePlace firstTouristSpotPlace = places.stream()
				.filter(p -> p.getPlaceType() == PlaceType.TouristSpot)
				.min(Comparator.comparing(SchedulePlace::getVisitStart))
				.orElse(null);
			String firstTheme = resolveThemeFromTouristSpot(firstTouristSpotPlace);

			scheduleSummaryList.add(
				ScheduleSummaryResBody.from(group.getGroupName(), firstRegionName, firstTheme,
					schedule.getStartSchedule(),
					schedule.getEndSchedule())
			);
		}

		return scheduleSummaryList;
	}

	// 일정 수정
	@Transactional
	public ScheduleResBody updateSchedule(Long scheduleId, ScheduleReqBody reqBody, Long userId) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new NoSuchElementException("존재하지 않는 일정입니다."));

		Group group = groupRepository.findByGroupName(reqBody.groupName())
			.orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

		// 그룹 창설자 검증
		groupService.validateGroupCreator(group.getGroupName(), userId);

		schedule.update(reqBody.startSchedule(), reqBody.endSchedule(), group);

		schedule.getSchedulePlaces().clear();

		for (SchedulePlaceReqBody places : reqBody.schedulePlaces()) {
			placeValidationService.validateContentIdByPlaceType(places.placeType(), places.contentId());
			// 변경됨
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
			throw new ResourceUpdateException("일정 수정에 실패했습니다.", e);
		}
	}

	// 일정 삭제
	@Transactional
	public void deleteSchedule(Long scheduleId, Long userId) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new NoSuchElementException("존재하지 않는 일정입니다."));

		groupService.validateGroupCreator(schedule.getGroup().getGroupName(), userId);

		try {
			scheduleRepository.delete(schedule);
		} catch (Exception e) {
			throw new ResourceDeletionException("일정 삭제에 실패했습니다.", e);
		}
	}

	/**
	 * placeType에 맞게 레포지토리에서 장소를 찾아 areaCode/siGunGuCode로 region을 구한다.
	 * 장소를 찾지 못하면 null 반환.
	 */
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

	/**
	 * TouristSpot이 있을 때만 theme을 추출. 없으면 "기타".
	 */
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
