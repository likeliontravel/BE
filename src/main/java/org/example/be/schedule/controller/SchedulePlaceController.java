package org.example.be.schedule.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.response.CommonResponse;
import org.example.be.schedule.dto.SchedulePlaceRequestDTO;
import org.example.be.schedule.dto.SchedulePlaceResponseDTO;
import org.example.be.schedule.service.SchedulePlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/schedule/detail")
@RequiredArgsConstructor
public class SchedulePlaceController {

    private final SchedulePlaceService schedulePlaceService;

    @PostMapping
    public ResponseEntity<CommonResponse<SchedulePlaceResponseDTO>> createSchedulePlace(
            @RequestBody SchedulePlaceRequestDTO requestDTO) {
        SchedulePlaceResponseDTO response = schedulePlaceService.createSchedulePlace(requestDTO);
        return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 생성 성공"));
    }

    @PutMapping("/{schedulePlaceId}")
    public ResponseEntity<CommonResponse<SchedulePlaceResponseDTO>> updateSchedulePlace(
            @PathVariable Long schedulePlaceId,
            @RequestBody SchedulePlaceRequestDTO requestDTO) {
        SchedulePlaceResponseDTO response = schedulePlaceService.updateSchedulePlace(schedulePlaceId, requestDTO);
        return ResponseEntity.ok(CommonResponse.success(response, "세부 일정 수정 성공"));
    }

    @DeleteMapping("/{schedulePlaceId}")
    public ResponseEntity<CommonResponse<Void>> deleteSchedulePlace(@PathVariable Long schedulePlaceId) {
        schedulePlaceService.deleteSchedulePlace(schedulePlaceId);
        return ResponseEntity.ok(CommonResponse.success(null, "세부 일정 삭제 성공"));
    }
}
