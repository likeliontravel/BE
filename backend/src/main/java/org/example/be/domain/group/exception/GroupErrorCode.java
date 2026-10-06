package org.example.be.domain.group.exception;

import org.example.be.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 그룹 도메인 에러 코드.
 * 그룹 생성, 조회, 그룹 권한, 초대링크, 그룹 공지
 */
@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {

	// --- 그룹 (Group) ---
	GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 그룹입니다."),
	GROUP_NAME_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 존재하는 그룹명입니다."),
	GROUP_NOT_CREATOR(HttpStatus.FORBIDDEN, "해당 그룹의 창설자만 접근할 수 있습니다."),
	GROUP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 그룹의 멤버가 아닙니다."),
	GROUP_ALREADY_MEMBER(HttpStatus.BAD_REQUEST, "이미 그룹에 속해 있는 사용자입니다."),
	GROUP_CREATOR_CANNOT_EXIT(HttpStatus.BAD_REQUEST, "그룹 창설자는 그룹을 나갈 수 없습니다. 그룹 삭제 기능을 이용해주세요."),

	// --- 초대 (Invitation) ---
	INVALID_INVITATION(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 초대 코드입니다."),
	INVITATION_EXPIRED(HttpStatus.BAD_REQUEST, "초대 링크가 만료되었습니다. 새로 생성하세요."),
	INVITATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "초대 링크가 없습니다. 초대 링크를 생성하세요."),

	// --- 공지 (Announcement) ---
	GROUP_ANNOUNCEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹 공지가 없습니다.");

	private final HttpStatus status;
	private final String message;

}
