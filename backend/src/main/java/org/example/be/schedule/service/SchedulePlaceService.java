package org.example.be.schedule.service;

import java.util.NoSuchElementException;

import org.example.be.exception.custom.ResourceCreationException;
import org.example.be.exception.custom.ResourceDeletionException;
import org.example.be.exception.custom.ResourceUpdateException;
import org.example.be.group.service.GroupService;
import org.example.be.schedule.dto.request.SchedulePlaceReqBody;
import org.example.be.schedule.dto.response.SchedulePlaceResBody;
import org.example.be.schedule.entity.SchedulePlace;
import org.example.be.schedule.repository.SchedulePlaceRepository;
import org.example.be.schedule.repository.ScheduleRepository;
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

	@Transactional
	public SchedulePlaceResBody createSchedulePlace(Long scheduleId, SchedulePlaceReqBody reqBody, Long userId) {
		var schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new NoSuchElementException("해당 일정이 존재하지 않습니다."));

		// 권한 검증: 그룹 창설자만 세부 일정을 추가할 수 있음
		groupService.validateGroupCreator(schedule.getGroup().getGroupName(), userId);

		placeValidationService.validateContentIdByPlaceType(reqBody.placeType(), reqBody.contentId());

		SchedulePlace schedulePlace = SchedulePlace.create(
			schedule,
			reqBody.contentId(),
			reqBody.placeType(),
			reqBody.visitStart(),
			reqBody.visitedEnd(),
			reqBody.dayOrder(),
			reqBody.orderInDay()
		);

		try {
			var saved = schedulePlaceRepository.save(schedulePlace);
			return SchedulePlaceResBody.from(saved);
		} catch (Exception e) {
			throw new ResourceCreationException("세부 일정 생성에 실패했습니다.", e);
		}
	}

	@Transactional
	public SchedulePlaceResBody updateSchedulePlace(Long placeId, SchedulePlaceReqBody reqBody, Long userId) {
		var place = schedulePlaceRepository.findById(placeId)
			.orElseThrow(() -> new NoSuchElementException("해당 세부 일정이 존재하지 않습니다."));

		// 권한 검증: 그룹 창설자만 세부 일정을 수정할 수 있음
		groupService.validateGroupCreator(place.getSchedule().getGroup().getGroupName(), userId);

		placeValidationService.validateContentIdByPlaceType(reqBody.placeType(), reqBody.contentId());

		place.update(
			reqBody.contentId(),
			reqBody.placeType(),
			reqBody.visitStart(),
			reqBody.visitedEnd(),
			reqBody.dayOrder(),
			reqBody.orderInDay()
		);

		try {
			return SchedulePlaceResBody.from(schedulePlaceRepository.save(place));
		} catch (Exception e) {
			throw new ResourceUpdateException("세부 일정 수정에 실패했습니다.", e);
		}
	}

	@Transactional
	public void deleteSchedulePlace(Long placeId, Long userId) {
		var place = schedulePlaceRepository.findById(placeId)
			.orElseThrow(() -> new NoSuchElementException("해당 세부 일정이 존재하지 않습니다."));

		// 권한 검증: 그룹 창설자만 세부 일정을 삭제할 수 있음
		groupService.validateGroupCreator(place.getSchedule().getGroup().getGroupName(), userId);

		try {
			schedulePlaceRepository.delete(place);
		} catch (Exception e) {
			throw new ResourceDeletionException("세부 일정 삭제에 실패했습니다.", e);
		}
	}
}
