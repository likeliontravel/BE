package org.example.be.schedule.controller;

import org.example.be.response.CommonResponse;
import org.example.be.schedule.dto.request.SchedulePlaceReqBody;
import org.example.be.schedule.dto.response.SchedulePlaceResBody;
import org.example.be.schedule.service.SchedulePlaceService;
import org.example.be.security.config.SecurityUser;
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
@RequestMapping("/schedule/detail")
@RequiredArgsConstructor
public class SchedulePlaceController {

	private final SchedulePlaceService schedulePlaceService;

	@PostMapping
	public ResponseEntity<CommonResponse<SchedulePlaceResBody>> createSchedulePlace(
		@Valid @RequestBody SchedulePlaceReqBody reqBody,
		@AuthenticationPrincipal SecurityUser securityUser) {
		SchedulePlaceResBody response = schedulePlaceService.createSchedulePlace(reqBody, securityUser.getId());
		return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 생성 성공"));
	}

	@PutMapping("/{schedulePlaceId}")
	public ResponseEntity<CommonResponse<SchedulePlaceResBody>> updateSchedulePlace(
		@PathVariable Long schedulePlaceId,
		@Valid @RequestBody SchedulePlaceReqBody reqBody,
		@AuthenticationPrincipal SecurityUser securityUser) {
		SchedulePlaceResBody response = schedulePlaceService.updateSchedulePlace(schedulePlaceId, reqBody,
			securityUser.getId());
		return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 수정 성공"));
	}

	@DeleteMapping("/{schedulePlaceId}")
	public ResponseEntity<CommonResponse<Void>> deleteSchedulePlace(
		@PathVariable Long schedulePlaceId,
		@AuthenticationPrincipal SecurityUser securityUser) {
		schedulePlaceService.deleteSchedulePlace(schedulePlaceId, securityUser.getId());
		return ResponseEntity.ok(CommonResponse.success(null, "세부 일정 삭제 성공"));
	}
}
