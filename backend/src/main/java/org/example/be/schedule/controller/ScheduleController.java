package org.example.be.schedule.controller;

import java.util.List;

import org.example.be.resolver.DecodedPathVariable;
import org.example.be.response.CommonResponse;
import org.example.be.schedule.dto.request.ScheduleReqBody;
import org.example.be.schedule.dto.response.ScheduleResBody;
import org.example.be.schedule.dto.response.ScheduleSummaryResBody;
import org.example.be.schedule.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor

public class ScheduleController {

	private final ScheduleService scheduleService;

	// 일정 생성하기
	@PostMapping
	public ResponseEntity<CommonResponse<ScheduleResBody>> createSchedule(@RequestBody ScheduleReqBody requestDTO) {
		ScheduleResBody response = scheduleService.createSchedule(requestDTO);
		return ResponseEntity.ok(CommonResponse.success(response, "일정 생성 성공"));
	}

	// 일정 조회하기
	@GetMapping("/get/{groupName}")
	public ResponseEntity<CommonResponse<ScheduleResBody>> getScheduleByGroupName(
		@DecodedPathVariable String groupName) {
		ScheduleResBody response = scheduleService.getScheduleByGroupName(groupName);
		return ResponseEntity.ok(CommonResponse.success(response, "일정 조회 성공"));
	}

	// 일정 목록 조회하기 - 일정 요약 정보를 목록으로 조회
	@GetMapping("/getList")
	public ResponseEntity<CommonResponse<List<ScheduleSummaryResBody>>> getUserScheduleSummaries() {
		List<ScheduleSummaryResBody> summaries = scheduleService.getScheduleList();
		return ResponseEntity.ok(CommonResponse.success(summaries, "일정 요약 목록 조회 성공"));
	}

	// 일정 수정하기
	@PutMapping("/{scheduleId}")
	public ResponseEntity<CommonResponse<ScheduleResBody>> updateSchedule(
		@PathVariable Long scheduleId,
		@RequestBody ScheduleReqBody requestDTO
	) {
		ScheduleResBody response = scheduleService.updateSchedule(scheduleId, requestDTO);
		return ResponseEntity.ok(CommonResponse.success(response, "일정 수정 성공"));
	}

	// 일정 삭제하기
	@DeleteMapping("/{scheduleId}")
	public ResponseEntity<CommonResponse<Void>> deleteSchedule(@PathVariable Long scheduleId) {
		scheduleService.deleteSchedule(scheduleId);
		return ResponseEntity.ok(CommonResponse.success(null, "일정 삭제 성공"));
	}
}
