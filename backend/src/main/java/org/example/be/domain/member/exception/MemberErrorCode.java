package org.example.be.domain.member.exception;

import org.example.be.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원 도메인 에러 코드.
 * 회원 가입, 조회, 로그인과 인증 토큰
 */
@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

	// --- 회원 (Member) ---
	EMAIL_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
	EMAIL_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "가입되지 않은 이메일입니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

	// --- 인증 토큰 / 로그인 (Auth Token) ---
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
	SOCIAL_ACCOUNT_LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "소셜 로그인으로 가입된 계정입니다. 소셜 로그인을 이용해주세요."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "손상되었거나 만료된 Refresh Token입니다.");

	private final HttpStatus status;
	private final String message;

}
