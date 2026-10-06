package org.example.be.global.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 특정 도메인에 속하지 않는 에러 코드.
 * 공통 또는 프레임워크 요청 오류, 인증/인가, 도메인 특정이 없는 리소스의 CRUD, 외부 연동
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

	// --- 공통 (Common) ---
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	INVALID_URI_VARIABLES(HttpStatus.BAD_REQUEST, "잘못된 파라미터 입력입니다."),

	// --- 프레임워크 요청 오류 (Request) ---
	// 스프링/톰캣이 던지는 예외 번역용. RequestExceptionHandler 가 쓴다.
	INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문 형식이 올바르지 않습니다."),
	MISSING_REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
	MISSING_REQUIRED_HEADER(HttpStatus.BAD_REQUEST, "필수 요청 헤더가 누락되었습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
	ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 경로입니다."),
	DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "이미 존재하거나 제약 조건에 위배되는 데이터입니다."),
	MISSING_REQUIRED_PART(HttpStatus.BAD_REQUEST, "필수 첨부 파일이 누락되었습니다."),
	INVALID_MULTIPART_REQUEST(HttpStatus.BAD_REQUEST, "파일 업로드 형식의 요청이 아닙니다."),
	FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 크기를 초과했습니다."),
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 요청 형식(Content-Type)입니다."),

	// --- 인증 / 인가 (Auth) ---
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

	// --- 리소스 CRUD ---
	RESOURCE_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리소스 생성에 실패했습니다."),
	RESOURCE_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리소스 수정에 실패했습니다."),
	RESOURCE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리소스 삭제에 실패했습니다."),

	// --- 외부 API 연동 (External) ---
	EXTERNAL_API_FAILED(HttpStatus.BAD_GATEWAY, "외부 서비스 연동에 실패했습니다.");

	private final HttpStatus status;
	private final String message;

}
