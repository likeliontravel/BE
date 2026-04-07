package org.example.be.global.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
	Error Code ENUM:
	에러 코드를 enum으로 관리.
	약 30개 이상이 되면 ErrorCode를 인터페이스로,
	ErrorCode를 interface로 변경하고, 도메인 별 ErrorCode 구현체 enum을 따로 만들어 관리

	INTERNAL_SERVER_ERROR는 사실 엄밀히 말하면 비즈니스 예외가 아니지만, 우선 처음 작성하는 현재 수준 차원에서 편의상 타협했습니다.
	우리가 프로젝트 계속 진행하면서 점차 아래 예시구조처럼 만들고자 합니당

	global/exception/code/
  	├── ErrorCode.java          ← 공통 (UNAUTHORIZED, FORBIDDEN, INTERNAL_SERVER_ERROR) <- 인터페이스 타입 , 도메인별 에러코드는 implements로 구현
  	├── MemberErrorCode.java    ← 회원 도메인
  	├── GroupErrorCode.java     ← 그룹 도메인
  	└── ScheduleErrorCode.java  ← 일정 도메인

  	이후 비즈니스 예외 / 시스템 예외를 구체화해나가고자 합니다.

 */

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// --- 공통 (Common) ---
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	INVALID_URI_VARIABLES(HttpStatus.BAD_REQUEST, "잘못된 파라미터 입력입니다."),

	// --- 인증 / 인가 (Auth) ---
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	USER_NOT_AUTHENTICATED(HttpStatus.INTERNAL_SERVER_ERROR, "인증된 사용자 정보를 불러올 수 없습니다."),

	// --- 회원 (Member) ---
	EMAIL_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
	EMAIL_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "가입되지 않은 이메일입니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

	// --- 그룹 (Group) ---
	GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 그룹입니다."),
	GROUP_NAME_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 존재하는 그룹명입니다."),
	GROUP_NOT_CREATOR(HttpStatus.FORBIDDEN, "해당 그룹의 창설자만 접근할 수 있습니다."),
	GROUP_MEMBER_NOT_FOUND(HttpStatus.FORBIDDEN, "해당 그룹의 멤버가 아닙니다."),
	GROUP_ALREADY_MEMBER(HttpStatus.BAD_REQUEST, "이미 그룹에 속해 있는 사용자입니다."),
	INVALID_INVITATION(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 초대 코드입니다."),
	INVITATION_EXPIRED(HttpStatus.BAD_REQUEST, "초대 링크가 만료되었습니다. 새로 생성하세요."),
	INVITATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "초대 링크가 없습니다. 초대 링크를 생성하세요."),
	GROUP_CREATOR_CANNOT_EXIT(HttpStatus.UNPROCESSABLE_ENTITY, "그룹 창설자는 그룹을 나갈 수 없습니다. 그룹 삭제 기능을 이용해주세요."),

	// --- 게시판 (Board) ---
	BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
	BOARD_NOT_WRITER(HttpStatus.FORBIDDEN, "글의 작성자만 접근할 수 있습니다."),
	INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "다른 게시물의 댓글에는 대댓글을 달 수 없습니다."),
	BOARD_TITLE_BLANK(HttpStatus.BAD_REQUEST, "게시글 제목은 비어있을 수 없습니다."),
	BOARD_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "게시글 내용은 비어있을 수 없습니다."),

	// --- 일정 (Schedule) ---
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 일정입니다."),
	SCHEDULE_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 그룹에 존재하는 일정이 있습니다."),

	// --- 장소 (Place) ---
	PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장소입니다."),
	INVALID_REGION(HttpStatus.BAD_REQUEST, "유효하지 않은 지역 값입니다."),
	INVALID_THEME(HttpStatus.BAD_REQUEST, "유효하지 않은 테마 값입니다."),

	// --- 리소스 CRUD ---
	RESOURCE_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리소스 생성에 실패했습니다."),
	RESOURCE_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리소스 수정에 실패했습니다."),
	RESOURCE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리소스 삭제에 실패했습니다."),

	// --- 파일 스토리지 (GCS) ---
	GCS_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
	GCS_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),
	INVALID_IMAGE_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다: 이미지 필요"),
	INVALID_VIDEO_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다: 영상 필요"),
	INVALID_RECORD_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다: 음원 또는 음성 필요");

	private final HttpStatus status;
	private final String message;

}
