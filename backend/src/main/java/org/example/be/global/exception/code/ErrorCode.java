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

	// --- 프레임워크 요청 오류 (Request) ---
	// 스프링/톰캣이 던지는 예외 번역용. RequestExceptionHandler가 쓴다.
	INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문 형식이 올바르지 않습니다."),
	MISSING_REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
	MISSING_REQUIRED_HEADER(HttpStatus.BAD_REQUEST, "필수 요청 헤더가 누락되었습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
	ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 경로입니다."),
	DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "이미 존재하거나 제약 조건에 위배되는 데이터입니다."),
	MISSING_REQUIRED_PART(HttpStatus.BAD_REQUEST, "필수 첨부 파일이 누락되었습니다."),
	INVALID_MULTIPART_REQUEST(HttpStatus.BAD_REQUEST, "파일 업로드 형식의 요청이 아닙니다."),

	// --- 인증 / 인가 (Auth) ---
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	USER_NOT_AUTHENTICATED(HttpStatus.INTERNAL_SERVER_ERROR, "인증된 사용자 정보를 불러올 수 없습니다."),

	// --- 회원 (Member) ---
	EMAIL_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
	EMAIL_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "가입되지 않은 이메일입니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

	// --- 인증 토큰 / 로그인 (Auth Token) ---
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
	SOCIAL_ACCOUNT_LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "소셜 로그인으로 가입된 계정입니다. 소셜 로그인을 이용해주세요."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "손상되었거나 만료된 Refresh Token입니다."),

	// --- 메일 (Mail) ---
	MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "인증 메일 발송에 실패했습니다."),
	MAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증 코드를 찾을 수 없거나 만료되었습니다."),
	MAIL_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),

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
	GROUP_ANNOUNCEMENT_LATEST_NOT_FOUND(HttpStatus.NO_CONTENT, "그룹 공지가 없어 최신 그룹 공지가 없습니다."),
	GROUP_ANNOUNCEMENT_NOT_FOUND(HttpStatus.NO_CONTENT, "그룹 공지가 없습니다."),

	// --- 채팅 (chat) ---
	GROUP_CHAT_NOT_FOUND(HttpStatus.NO_CONTENT, "해당 그룹에 아직 메시지가 존재하지 않습니다."),
	CHAT_PREVIOUS_MESSAGE_NOT_FOUND(HttpStatus.NO_CONTENT, "이전 메시지가 더 존재하지 않습니다."),

	// --- 게시판 (Board) ---
	BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
	BOARD_NOT_WRITER(HttpStatus.FORBIDDEN, "게시글의 작성자만 접근할 수 있습니다."),
	COMMENT_NOT_WRITER(HttpStatus.FORBIDDEN, "댓글의 작성자만 접근할 수 있습니다."),
	INVALID_PARENT_COMMENT_OF_BOARD(HttpStatus.BAD_REQUEST, "다른 게시물의 댓글에는 대댓글을 달 수 없습니다."),
	INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "부모 댓글을 찾을 수 없습니다."),
	BOARD_TITLE_BLANK(HttpStatus.BAD_REQUEST, "게시글 제목은 비어있을 수 없습니다."),
	BOARD_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "게시글 내용은 비어있을 수 없습니다."),
	BOARD_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "게시글 이미지 업로드 가능 개수를 초과했습니다."),
	BOARD_IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 이미지가 없습니다."),

	// --- 일정 (Schedule) ---
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "일정이 존재하지 않습니다."),
	SCHEDULE_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 그룹에 존재하는 일정이 있습니다."),
	SCHEDULE_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "세부 일정이 존재하지 않습니다."),
	SCHEDULE_PLACE_DUPLICATE_ORDER(HttpStatus.BAD_REQUEST, "같은 날짜에 동일한 방문 순서가 중복되었습니다."),
	SCHEDULE_PLACE_DUPLICATE_ID(HttpStatus.BAD_REQUEST, "요청에 동일한 세부 일정 ID (schedulePlaceId) 가 중복되었습니다."),
	SCHEDULE_INVALID_PERIOD(HttpStatus.BAD_REQUEST, "일정 기간이 유효하지 않습니다. (시작일이 종료일보다 늦을 수 없습니다.)"),

	// --- 장소 (Place) ---
	PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장소입니다."),
	INVALID_REGION(HttpStatus.BAD_REQUEST, "유효하지 않은 지역 값입니다."),
	INVALID_THEME(HttpStatus.BAD_REQUEST, "유효하지 않은 테마 값입니다."),

	// --- 리소스 CRUD ---
	RESOURCE_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리소스 생성에 실패했습니다."),
	RESOURCE_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리소스 수정에 실패했습니다."),
	RESOURCE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리소스 삭제에 실패했습니다."),

	// --- 외부 API 연동 (External) ---
	EXTERNAL_API_FAILED(HttpStatus.BAD_GATEWAY, "외부 서비스 연동에 실패했습니다."),

	// --- 파일 스토리지 (GCS) ---
	GCS_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
	GCS_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),
	INVALID_IMAGE_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다: 이미지 필요"),
	INVALID_VIDEO_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다: 영상 필요"),
	INVALID_RECORD_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다: 음원 또는 음성 필요"),
	FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 크기를 초과했습니다."),

	// --- 알림	(Notification) ---
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),
	NOTIFICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 알림만 접근할 수 있습니다."),
	NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송에 실패했습니다.");

	private final HttpStatus status;
	private final String message;

}
