package org.example.be.domain.schedule.controller;

import java.util.List;

import org.example.be.domain.schedule.dto.request.SchedulePlaceReqBody;
import org.example.be.domain.schedule.dto.request.SchedulePlaceUpdateReqBody;
import org.example.be.domain.schedule.dto.response.SchedulePlaceResBody;
import org.example.be.domain.schedule.service.SchedulePlaceService;
import org.example.be.global.response.CommonResponse;
import org.example.be.global.security.config.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class SchedulePlaceController {

	private final SchedulePlaceService schedulePlaceService;

	// 특정 일정(scheduleId)에 세부 장소 추가
	@PostMapping("/detail/{scheduleId}")
	public ResponseEntity<CommonResponse<List<SchedulePlaceResBody>>> createSchedulePlaces(
		@PathVariable Long scheduleId,
		@Valid @RequestBody List<SchedulePlaceReqBody> reqBodies,
		@AuthenticationPrincipal SecurityUser securityUser) {
		List<SchedulePlaceResBody> response = schedulePlaceService.createSchedulePlaces(scheduleId, reqBodies,
			securityUser.getId());
		return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 생성 성공"));
	}

	// 특정 일정(scheduleId)의 세부 장소 전체 상태를 일괄 수정 (추가 / 수정 / 삭제 / 순서변경 일괄 반영)
	// 바디: 편집된 전체 장소 목록. 기존 블록이면 schedulePlaceId 포함, 신규면 null, 빈 배열: 전체 삭제
	@PutMapping("/detail/{scheduleId}")
	public ResponseEntity<CommonResponse<List<SchedulePlaceResBody>>> updateSchedulePlaces(
		@PathVariable Long scheduleId,
		@Valid @RequestBody List<SchedulePlaceUpdateReqBody> reqBodies,
		@AuthenticationPrincipal SecurityUser securityUser) {
		List<SchedulePlaceResBody> response = schedulePlaceService.updateSchedulePlaces(scheduleId, reqBodies,
			securityUser.getId());
		return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 일괄 수정 성공"));
	}

	// TODO: 단건 수정 / 삭제 보존 여부 결정
	// 특정 세부 장소(schedulePlaceId) 단건 수정
	// 단건 수정은 리스트 수정과 겹치지 않도록 'single' 세그먼트로 분리
	@PutMapping("/detail/single/{schedulePlaceId}")
	public ResponseEntity<CommonResponse<SchedulePlaceResBody>> updateSchedulePlace(
		@PathVariable Long schedulePlaceId,
		@Valid @RequestBody SchedulePlaceReqBody reqBody,
		@AuthenticationPrincipal SecurityUser securityUser) {
		SchedulePlaceResBody response = schedulePlaceService.updateSchedulePlace(schedulePlaceId, reqBody,
			securityUser.getId());
		return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 수정 성공"));
	}

	// 특정 세부 장소(schedulePlaceId) 삭제
	// 단건 삭제는 리스트 수정과 겹치지 않도록 'single' 세그먼트로 분리, 리스트 수정에서 삭제 가능, 전체 삭제는 빈 리스트 요청
	@DeleteMapping("/detail/single/{schedulePlaceId}")
	public ResponseEntity<CommonResponse<Void>> deleteSchedulePlace(
		@PathVariable Long schedulePlaceId,
		@AuthenticationPrincipal SecurityUser securityUser) {
		schedulePlaceService.deleteSchedulePlace(schedulePlaceId, securityUser.getId());
		return ResponseEntity.ok(CommonResponse.success(null, "세부 일정 삭제 성공"));
	}
}
