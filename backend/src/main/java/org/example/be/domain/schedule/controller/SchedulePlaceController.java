package org.example.be.domain.schedule.controller;

import java.util.List;

import org.example.be.domain.schedule.dto.request.SchedulePlaceCreateListReqBody;
import org.example.be.domain.schedule.dto.request.SchedulePlaceUpdateListReqBody;
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
		@Valid @RequestBody SchedulePlaceCreateListReqBody reqBody,
		@AuthenticationPrincipal SecurityUser securityUser) {
		List<SchedulePlaceResBody> response = schedulePlaceService.createSchedulePlaces(scheduleId,
			reqBody.schedulePlaces(),
			securityUser.getId());
		return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 생성 성공"));
	}

	// 특정 일정(scheduleId)의 세부 장소 전체 상태를 일괄 수정 (추가 / 수정 / 삭제 / 순서변경 일괄 반영)
	// 바디: 편집된 전체 장소 목록. 기존 블록이면 schedulePlaceId 포함, 신규면 null, 빈 배열: 전체 삭제
	@PutMapping("/detail/{scheduleId}")
	public ResponseEntity<CommonResponse<List<SchedulePlaceResBody>>> updateSchedulePlaces(
		@PathVariable Long scheduleId,
		@Valid @RequestBody SchedulePlaceUpdateListReqBody reqBody,
		@AuthenticationPrincipal SecurityUser securityUser) {
		List<SchedulePlaceResBody> response = schedulePlaceService.updateSchedulePlaces(scheduleId,
			reqBody.schedulePlaces(),
			securityUser.getId());
		return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 일괄 수정 성공"));
	}

	// 일정 블록 전체 삭제
	// 단건 삭제는 리스트 수정에서 삭제 가능, 전체 삭제는 해당 일정의 scheduleId만 받아 처리
	@DeleteMapping("/detail/{scheduleId}")
	public ResponseEntity<CommonResponse<Void>> deleteAllSchedulePlaces(
		@PathVariable Long scheduleId,
		@AuthenticationPrincipal SecurityUser securityUser) {
		schedulePlaceService.deleteAllSchedulePlaces(scheduleId, securityUser.getId());
		return ResponseEntity.ok(CommonResponse.success(null, "세부 일정 삭제 성공"));
	}
}
