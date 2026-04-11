package org.example.be.global.exception;

import org.example.be.global.exception.code.ErrorCode;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	// 디버깅용 추가 메시지 (클라이언트에는 ErrorCode 메시지만 노출)
	public BusinessException(ErrorCode errorCode, String debugMessage) {
		super(debugMessage);
		this.errorCode = errorCode;
	}
}
