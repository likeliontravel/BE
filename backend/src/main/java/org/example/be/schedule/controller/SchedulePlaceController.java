package org.example.be.schedule.controller;

import org.example.be.response.CommonResponse;
import org.example.be.schedule.dto.request.SchedulePlaceReqBody;
import org.example.be.schedule.dto.response.SchedulePlaceResBody;
import org.example.be.schedule.service.SchedulePlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/schedule/detail")
@RequiredArgsConstructor
public class SchedulePlaceController {

	private final SchedulePlaceService schedulePlaceService;

	@PostMapping
	public ResponseEntity<CommonResponse<SchedulePlaceResBody>> createSchedulePlace(
		@RequestBody SchedulePlaceReqBody requestDTO) {
		SchedulePlaceResBody response = schedulePlaceService.createSchedulePlace(requestDTO);
		return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 생성 성공"));
	}

	@PutMapping("/{schedulePlaceId}")
	public ResponseEntity<CommonResponse<SchedulePlaceResBody>> updateSchedulePlace(
		@PathVariable Long schedulePlaceId,
		@RequestBody SchedulePlaceReqBody requestDTO) {
		SchedulePlaceResBody response = schedulePlaceService.updateSchedulePlace(schedulePlaceId, requestDTO);
		return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 수정 성공"));
	}

	@DeleteMapping("/{schedulePlaceId}")
	public ResponseEntity<CommonResponse<Void>> deleteSchedulePlace(@PathVariable Long schedulePlaceId) {
		schedulePlaceService.deleteSchedulePlace(schedulePlaceId);
		return ResponseEntity.ok(CommonResponse.success(null, "세부 일정 삭제 성공"));
	}
}
