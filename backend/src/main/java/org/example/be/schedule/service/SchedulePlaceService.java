package org.example.be.schedule.service;

import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
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
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "scheduleId: " + scheduleId));

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
			throw new BusinessException(ErrorCode.RESOURCE_CREATION_FAILED, "세부 일정 생성 실패 - message: " + e.getMessage());
		}
	}

	@Transactional
	public SchedulePlaceResBody updateSchedulePlace(Long placeId, SchedulePlaceReqBody reqBody, Long userId) {
		var place = schedulePlaceRepository.findById(placeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_PLACE_NOT_FOUND, "placeId: " + placeId));

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
			throw new BusinessException(ErrorCode.RESOURCE_UPDATE_FAILED, "세부 일정 수정 실패 - message: " + e.getMessage());
		}
	}

	@Transactional
	public void deleteSchedulePlace(Long placeId, Long userId) {
		var place = schedulePlaceRepository.findById(placeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_PLACE_NOT_FOUND, "placeId: " + placeId));

		// 권한 검증: 그룹 창설자만 세부 일정을 삭제할 수 있음
		groupService.validateGroupCreator(place.getSchedule().getGroup().getGroupName(), userId);

		try {
			schedulePlaceRepository.delete(place);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.RESOURCE_DELETE_FAILED, "세부 일정 삭제 실패 - message: " + e.getMessage());
		}
	}
}
