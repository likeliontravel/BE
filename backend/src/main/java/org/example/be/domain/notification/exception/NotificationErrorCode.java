package org.example.be.domain.notification.exception;

import org.example.be.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 도메인 에러 코드.
 * 알림 조회와 수신자 권한
 */
@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

	// --- 알림 (Notification) ---
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),
	NOTIFICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 알림만 접근할 수 있습니다."),
	NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송에 실패했습니다.");

	private final HttpStatus status;
	private final String message;

}
