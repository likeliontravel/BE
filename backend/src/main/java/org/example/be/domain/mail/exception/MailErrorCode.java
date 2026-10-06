package org.example.be.domain.mail.exception;

import org.example.be.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 메일 도메인 에러 코드
 * 인증 메일 발송과 인증 코드 검증
 */
@Getter
@RequiredArgsConstructor
public enum MailErrorCode implements ErrorCode {

	// --- 메일 (Mail) ---
	MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "인증 메일 발송에 실패했습니다."),
	MAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증 코드를 찾을 수 없거나 만료되었습니다."),
	MAIL_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다.");

	private final HttpStatus status;
	private final String message;

}
