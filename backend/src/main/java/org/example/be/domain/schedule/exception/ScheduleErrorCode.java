package org.example.be.domain.schedule.exception;

import org.example.be.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 일정 도메인 에러 코드.
 * 일정과 세부일정(일정 장소)의 조회, 생성, 수정 검증
 */

@Getter
@RequiredArgsConstructor
public enum ScheduleErrorCode implements ErrorCode {

	// --- 일정 (Schedule) ---
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "일정이 존재하지 않습니다."),
	SCHEDULE_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 그룹에 존재하는 일정이 있습니다."),
	SCHEDULE_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "세부 일정이 존재하지 않습니다."),
	SCHEDULE_PLACE_DUPLICATE_ORDER(HttpStatus.BAD_REQUEST, "같은 날짜에 동일한 방문 순서가 중복되었습니다."),
	SCHEDULE_PLACE_DUPLICATE_ID(HttpStatus.BAD_REQUEST, "요청에 동일한 세부 일정 ID (schedulePlaceId) 가 중복되었습니다."),
	SCHEDULE_INVALID_PERIOD(HttpStatus.BAD_REQUEST, "일정 기간이 유효하지 않습니다. (시작일이 종료일보다 늦을 수 없습니다.)");

	private final HttpStatus status;
	private final String message;

}
