package org.example.be.member.controller;

import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.mail.dto.MailVerifyReqBody;
import org.example.be.mail.service.MailService;
import org.example.be.member.dto.request.MemberJoinReqBody;
import org.example.be.member.dto.request.MemberLoginReqBody;
import org.example.be.member.dto.request.PasswordResetReqBody;
import org.example.be.member.dto.request.PasswordResetSendReqBody;
import org.example.be.member.dto.response.MemberDto;
import org.example.be.member.entity.Member;
import org.example.be.member.service.AuthTokenService;
import org.example.be.member.service.MemberService;
import org.example.be.member.service.RefreshTokenStore;
import org.example.be.response.CommonResponse;
import org.example.be.security.config.SecurityUser;
import org.example.be.web.CookieHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final MemberService memberService;
	private final AuthTokenService authTokenService;
	private final CookieHelper cookieHelper;
	private final RefreshTokenStore refreshTokenStore;
	private final GroupInvitationService groupInvitationService;
	private final GroupService groupService;
	private final MailService mailService;

	// 회원가입
	@PostMapping("/join")
	public ResponseEntity<CommonResponse<MemberDto>> join(
		@Valid @RequestBody MemberJoinReqBody reqBody,
		HttpServletRequest request,
		HttpServletResponse response) {

		Member member = memberService.join(reqBody);

		String invitationCode = extractPendingInvitationCode(request);
		if (invitationCode != null) {
			try {
				GroupInvitation invitation = groupInvitationService.getValidInvitation(invitationCode);
				groupService.addMemberToGroup(invitation.getGroup().getGroupName(), member.getId());
			} catch (Exception e) {
				log.warn("회원가입 후 그룹 자동 가입 실패: {}", e.getMessage());
			} finally {
				deletePendingInvitationCodeCookie(response);
			}
		}

		MemberDto memberDto = MemberDto.from(member);
		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(memberDto, "회원가입 성공"));
	}

	// 로그인
	@PostMapping("/login")
	public ResponseEntity<CommonResponse<MemberDto>> login(
		@Valid @RequestBody MemberLoginReqBody reqBody,
		HttpServletRequest request,
		HttpServletResponse response) {

		Member member = memberService.authenticateAndGetMember(reqBody.email(), reqBody.password());

		issueTokensAndSetCookies(member);

		String invitationCode = extractPendingInvitationCode(request);
		if (invitationCode != null) {
			try {
				GroupInvitation invitation = groupInvitationService.getValidInvitation(invitationCode);
				groupService.addMemberToGroup(invitation.getGroup().getGroupName(), member.getId());
			} catch (Exception e) {
				log.warn("로그인 후 그룹 자동 가입 실패: {}", e.getMessage());
			} finally {
				deletePendingInvitationCodeCookie(response);
			}
		}

		MemberDto memberDto = MemberDto.from(member);
		return ResponseEntity.ok(CommonResponse.success(memberDto, "로그인 성공"));
	}

	// 로그아웃
	@PostMapping("/logout")
	public ResponseEntity<CommonResponse<Void>> logout(@AuthenticationPrincipal SecurityUser user) {
		refreshTokenStore.revokeAllByUserId(user.getId());
		cookieHelper.deleteCookie("accessToken");
		cookieHelper.deleteCookie("refreshToken");
		return ResponseEntity.ok(CommonResponse.success(null, "로그아웃 성공"));
	}

	// 비밀번호 초기화 요청
	@PostMapping("/password/reset/request")
	public ResponseEntity<CommonResponse<Void>> requestPasswordReset(
		@Valid @RequestBody PasswordResetSendReqBody reqBody) {
		mailService.sendPasswordResetMail(reqBody.email());
		return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 초기화 이메일 발송 성공"));
	}

	// 비밀번호 초기화
	@PostMapping("/password/reset")
	public ResponseEntity<CommonResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetReqBody reqBody) {
		mailService.verifyCode(new MailVerifyReqBody(reqBody.email(), reqBody.code()));
		memberService.resetPassword(reqBody.email(), reqBody.newPassword());
		return ResponseEntity.ok(CommonResponse.success(null, "비밀번호 초기화 성공"));
	}

	private void issueTokensAndSetCookies(Member member) {
		String accessToken = authTokenService.genAccessToken(member);
		String refreshToken = authTokenService.RefreshToken(member);
		cookieHelper.setCookie("accessToken", accessToken);
		cookieHelper.setCookie("refreshToken", refreshToken);
	}

	private String extractPendingInvitationCode(HttpServletRequest request) {
		if (request.getCookies() == null)
			return null;

		for (Cookie cookie : request.getCookies()) {
			if ("pendingInvitationCode".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private void deletePendingInvitationCodeCookie(HttpServletResponse response) {
		ResponseCookie deleteCookie = ResponseCookie.from("pendingInvitationCode", "")
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(0)
			.sameSite("Lax")
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
	}
}
