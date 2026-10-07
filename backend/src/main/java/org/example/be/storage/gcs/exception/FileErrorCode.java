package org.example.be.storage.gcs.exception;

import org.example.be.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 스토리지 (GCS) 에러 코드.
 * 업로드, 삭제  실패와 업로드 파일 형식 검증
 *
 * 업로드 크기 상한 초과 (FILE_SIZE_EXCEEDED, 413)는 톰캣 multipart 상한을 번역하는 RequestExceptionHandler 가 던지므로 CommonErrorCode 에 있다.
 */
@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {

	// --- 파일 스토리지 (GCS) ---
	GCS_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
	GCS_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),
	INVALID_IMAGE_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다: 이미지 필요"),
	INVALID_VIDEO_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다: 영상 필요"),
	INVALID_RECORD_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다: 음원 또는 음성 필요");

	private final HttpStatus status;
	private final String message;

}
