package org.example.be.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.response.CommonResponse;
import org.example.be.schedule.dto.ScheduleRequestDTO;
import org.example.be.schedule.dto.ScheduleResponseDTO;
import org.example.be.schedule.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor

public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<CommonResponse<ScheduleResponseDTO>> createSchedule(@RequestBody ScheduleRequestDTO requestDTO) {
        ScheduleResponseDTO response = scheduleService.createSchedule(requestDTO);
        return ResponseEntity.ok(CommonResponse.success(response, "일정 생성 성공"));
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<CommonResponse<ScheduleResponseDTO>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleRequestDTO requestDTO
    ) {
        ScheduleResponseDTO response = scheduleService.updateSchedule(scheduleId, requestDTO);
        return ResponseEntity.ok(CommonResponse.success(response, "일정 수정 성공"));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<CommonResponse<Void>> deleteSchedule(@PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(CommonResponse.success(null, "일정 삭제 성공"));
    }
}
