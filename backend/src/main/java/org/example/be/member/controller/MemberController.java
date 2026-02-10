package org.example.be.member.controller;

import org.example.be.member.dto.MemberDto;
import org.example.be.member.dto.MemberJoinReqBody;
import org.example.be.member.dto.MemberLoginReqBody;
import org.example.be.member.dto.MemberLoginResBody;
import org.example.be.member.dto.PasswordUpdateReqBody;
import org.example.be.member.entity.Member;
import org.example.be.member.service.AuthTokenService;
import org.example.be.member.service.MemberService;
import org.example.be.member.service.RefreshTokenStore;
import org.example.be.response.CommonResponse;
import org.example.be.security.config.SecurityUser;
import org.example.be.web.CookieHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;
	private final AuthTokenService authTokenService;
	private final CookieHelper cookieHelper;
	private final RefreshTokenStore refreshTokenStore;

	@PostMapping
	public ResponseEntity<CommonResponse<MemberDto>> join(@Valid @RequestBody MemberJoinReqBody reqBody) {

		Member member = memberService.join(reqBody);
		MemberDto memberDto = MemberDto.from(member);
		return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(memberDto, "회원가입 성공"));
	}

	@PostMapping("/login")
	public ResponseEntity<CommonResponse<MemberLoginResBody>> login(@Valid @RequestBody MemberLoginReqBody reqBody) {
		Member member = memberService.authenticateAndGetMember(reqBody.email(), reqBody.password());

		issueTokensAndSetCookies(member);

		MemberDto memberDto = MemberDto.from(member);
		MemberLoginResBody resBody = new MemberLoginResBody(memberDto);
		return ResponseEntity.ok(CommonResponse.success(resBody, "로그인 성공"));
	}

	@PostMapping("/logout")
	public ResponseEntity<CommonResponse<Void>> logout() {
		revokeRefreshTokenAndClearCookies();

		return ResponseEntity.ok(CommonResponse.success(null, "로그아웃 성공"));
	}

	@GetMapping("/profile")
	public ResponseEntity<CommonResponse<MemberDto>> profile(@AuthenticationPrincipal SecurityUser securityUser) {
		Member member = memberService.getById(securityUser.getId());
		MemberDto memberDto = MemberDto.from(member);
		return ResponseEntity.ok(CommonResponse.success(memberDto, "회원 프로필 조회 성공"));
	}

	// 비밀번호 변경
	@PutMapping("/passwordUpdate")
	public ResponseEntity<CommonResponse<String>> updatePassword(
		@RequestBody PasswordUpdateReqBody passwordUpdateReqBody) {
		memberService.updatePassword(passwordUpdateReqBody);
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "비밀번호 변경 성공"));
	}

	private void revokeRefreshTokenAndClearCookies() {
		String refreshToken = cookieHelper.getCookieValue("refreshToken", null);

		if (refreshToken != null || !refreshToken.isBlank()) {
			refreshTokenStore.revokeRefresh(refreshToken);
		}

		cookieHelper.deleteCookie("accessToken");
		cookieHelper.deleteCookie("refreshToken");
	}

	private void issueTokensAndSetCookies(Member member) {
		String accessToken = authTokenService.genAccessToken(member);
		String refreshToken = authTokenService.RefreshToken(member);
		cookieHelper.setCookie("accessToken", accessToken);
		cookieHelper.setCookie("refreshToken", refreshToken);
	}
}
