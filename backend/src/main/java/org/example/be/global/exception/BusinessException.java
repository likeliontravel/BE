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

	/**
	 * 원인 예외를 함께 보존하는 생성자.
	 *
	 * 외부 SDK·API 경계에서 예외를 변환할 때 반드시 이 생성자를 쓴다.
	 * cause를 넘기지 않으면 원본 스택트레이스가 변환 지점에서 소멸해,
	 * 로그에는 "리소스 수정에 실패했습니다"만 남고 진짜 원인(타임아웃·인증 실패 등)을 알 수 없다.
	 */
	public BusinessException(ErrorCode errorCode, String debugMessage, Throwable cause) {
		super(debugMessage, cause);
		this.errorCode = errorCode;
	}
}
