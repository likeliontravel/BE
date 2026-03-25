package org.example.be.member.controller;

import java.io.IOException;

import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.member.dto.request.MemberJoinReqBody;
import org.example.be.member.dto.request.MemberLoginReqBody;
import org.example.be.member.dto.request.MemberNameUpdateReqBody;
import org.example.be.member.dto.request.MemberPolicyUpdateReqBody;
import org.example.be.member.dto.request.MemberSubscribedUpdateReqBody;
import org.example.be.member.dto.request.PasswordUpdateReqBody;
import org.example.be.member.dto.response.MemberDto;
import org.example.be.member.dto.response.MemberProfileResBody;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;
	private final AuthTokenService authTokenService;
	private final CookieHelper cookieHelper;
	private final RefreshTokenStore refreshTokenStore;
	private final GroupInvitationService groupInvitationService;
	private final GroupService groupService;

	// 그룹 가입 로직 추가에 대한 설명입니다. :
	//
	// 비회원이 그룹 초대 링크를 통해 회원가입하게 되는 경우
	// pendingInvitationCode 쿠키 확인
	//
	// 쿠키에 pendingInvitationCode가 있다면 아래 순서로 동작
	// 회원가입 또는 로그인 -> 자동 그룹 가입 처리 -> 쿠키 삭제
	// 중간에 발생할 수 있는 에러 처리 흐름은 메서드 내 주석 확인
	// 로그인에 대한 처리는 CustomSuccessHandler(소셜) / MemberController.login()(일반) 에서 로직 삽입

	// 일반 회원가입
	@PostMapping
	public ResponseEntity<CommonResponse<MemberDto>> join(
		@Valid @RequestBody MemberJoinReqBody reqBody,
		HttpServletRequest request,
		HttpServletResponse response) {

		Member member = memberService.join(reqBody);

		// 초대 코드가 담긴 쿠키가 있는지 확인
		// 비로그인 상태에서 그룹 초대 링크 클릭 시 InvitationJoinController가 쿠키를 설정하고 로그인페이지로 리다이렉트시킴
		String invitationCode = extractPendingInvitationCode(request);
		if (invitationCode != null) {
			try {
				GroupInvitation invitation = groupInvitationService.getValidInvitation(invitationCode);
				groupService.addMemberToGroup(invitation.getGroup().getGroupName(), member.getId());
			} catch (Exception e) {
				// 그룹 자동 가입 실패 시 회원가입 자체는 유지
				log.warn("회원가입 후 그룹 자동 가입 실패: {}", e.getMessage());
			} finally {
				// 가입 성공/실패 여부 무관하게 쿠키 즉시 삭제
				deletePendingInvitationCodeCookie(response);
			}
		}

		MemberDto memberDto = MemberDto.from(member);
		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(memberDto, "회원가입 성공"));
	}

	// 로그인 (일반)
	@PostMapping("/login")
	public ResponseEntity<CommonResponse<MemberDto>> login(
		@Valid @RequestBody MemberLoginReqBody reqBody,
		HttpServletRequest request,
		HttpServletResponse response) {

		Member member = memberService.authenticateAndGetMember(reqBody.email(), reqBody.password());

		issueTokensAndSetCookies(member);

		// pendingInvitationCode 쿠키 확인 (비로그인 상태에서 초대링크 클릭 후 일반 로그인한 경우)
		String invitationCode = extractPendingInvitationCode(request);
		if (invitationCode != null) {
			try {
				GroupInvitation invitation = groupInvitationService.getValidInvitation(invitationCode);
				groupService.addMemberToGroup(invitation.getGroup().getGroupName(), member.getId());
			} catch (Exception e) {
				// 그룹 자동 가입 실패 시 로그인 자체는 유지
				log.warn("로그인 후 그룹 자동 가입 실패: {}", e.getMessage());
			} finally {
				// 가입 성공/실패 여부 무관하게 쿠키 즉시 삭제
				deletePendingInvitationCodeCookie(response);
			}
		}

		MemberDto memberDto = MemberDto.from(member);

		return ResponseEntity.ok(CommonResponse.success(memberDto, "로그인 성공"));
	}

	@PostMapping("/logout")
	public ResponseEntity<CommonResponse<Void>> logout(@AuthenticationPrincipal SecurityUser user) {

		refreshTokenStore.revokeAllByUserId(user.getId());

		cookieHelper.deleteCookie("accessToken");
		cookieHelper.deleteCookie("refreshToken");

		return ResponseEntity.ok(CommonResponse.success(null, "로그아웃 성공"));
	}

	@GetMapping("/me")
	public ResponseEntity<CommonResponse<MemberDto>> me(@AuthenticationPrincipal SecurityUser user) {

		Member member = memberService.getById(user.getId());
		MemberDto memberDto = MemberDto.from(member);
		return ResponseEntity.ok(CommonResponse.success(memberDto, "회원 프로필 조회 성공"));
	}

	// 비밀번호 변경(찾기 기능 x, 로그인한 상태에서만 변경 가능, 마이페이지에서 가능)
	@PutMapping("/me/passwordUpdate")
	public ResponseEntity<CommonResponse<String>> updatePassword(@RequestBody PasswordUpdateReqBody reqBody,
		@AuthenticationPrincipal SecurityUser user) {

		memberService.updatePassword(user.getId(), reqBody);
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "비밀번호 변경 성공"));
	}

	@PatchMapping("/me/name")
	public ResponseEntity<CommonResponse<String>> updateName(@Valid @RequestBody MemberNameUpdateReqBody reqBody,
		@AuthenticationPrincipal SecurityUser user) {

		memberService.updateName(user.getId(), reqBody.name());
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "회원 이름 변경 성공"));
	}

	@PostMapping("/me/profileImage")
	public ResponseEntity<CommonResponse<String>> updateProfileImage(@RequestParam MultipartFile file,
		@AuthenticationPrincipal SecurityUser user) throws IOException {

		String profileImageUrl = memberService.updateProfileImageUrl(user.getId(), file);
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(profileImageUrl, "프로필 사진 변경 성공"));
	}

	@DeleteMapping("/me/profileImage")
	public ResponseEntity<CommonResponse<Void>> deleteProfileImage(@AuthenticationPrincipal SecurityUser user) {

		memberService.deleteProfileImage(user.getId());
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "프로필 사진 삭제 성공"));
	}

	@PatchMapping("/me/policy")
	public ResponseEntity<CommonResponse<Void>> updatePolicyAgreed(
		@Valid @RequestBody MemberPolicyUpdateReqBody reqBody,
		@AuthenticationPrincipal SecurityUser user) {

		memberService.updatePolicyAgreed(user.getId(), reqBody.policyAgreed());
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "이용약관 동의 여부 변경 성공"));
	}

	@PatchMapping("/me/subscribed")
	public ResponseEntity<CommonResponse<Void>> updateSubscribed(
		@Valid @RequestBody MemberSubscribedUpdateReqBody reqBody,
		@AuthenticationPrincipal SecurityUser user) {

		memberService.updateSubscribed(user.getId(), reqBody.subscribed());
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "유료구독 여부 변경 성공"));
	}

	@DeleteMapping("/me")
	public ResponseEntity<CommonResponse<String>> deleteMember(@AuthenticationPrincipal SecurityUser user) {
		memberService.deleteMember(user.getId());

		refreshTokenStore.revokeAllByUserId(user.getId());

		cookieHelper.deleteCookie("accessToken");
		cookieHelper.deleteCookie("refreshToken");

		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "회원 탈퇴 성공"));

	}

	@GetMapping("/getNameAndImage/{memberId}")
	public ResponseEntity<CommonResponse<MemberProfileResBody>> getNameAndImage(@PathVariable Long memberId) {
		MemberProfileResBody resBody = memberService.getMemberProfile(memberId);
		return ResponseEntity.ok(CommonResponse.success(resBody, "회원 이름과 프로필 이미지 조회 성공"));
	}

	private void issueTokensAndSetCookies(Member member) {

		String accessToken = authTokenService.genAccessToken(member);
		String refreshToken = authTokenService.RefreshToken(member);

		cookieHelper.setCookie("accessToken", accessToken);
		cookieHelper.setCookie("refreshToken", refreshToken);
	}

	// HttpServletRequest에서 pendingInvitationCode 쿠키 값 추출
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

	// pendingInvitationCode 쿠키 즉시 삭제
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
