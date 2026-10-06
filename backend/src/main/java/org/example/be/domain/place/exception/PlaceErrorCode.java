package org.example.be.domain.place.exception;

import org.example.be.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 장소 도메인 에러 코드.
 * 관광지, 식당, 숙소 조회와 지역, 테마 값 검증 (일정, 게시판, TourAPI 배치가 함께 쓴다)
 */
@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements ErrorCode {

	// --- 장소 (Place) ---
	PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장소입니다."),
	INVALID_REGION(HttpStatus.BAD_REQUEST, "유효하지 않은 지역 값입니다."),
	INVALID_THEME(HttpStatus.BAD_REQUEST, "유효하지 않은 테마 값입니다.");

	private final HttpStatus status;
	private final String message;

}
