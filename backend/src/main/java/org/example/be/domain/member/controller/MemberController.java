package org.example.be.domain.member.controller;

import java.io.IOException;

import org.example.be.domain.member.dto.request.MemberNameUpdateReqBody;
import org.example.be.domain.member.dto.request.MemberPolicyUpdateReqBody;
import org.example.be.domain.member.dto.request.MemberSubscribedUpdateReqBody;
import org.example.be.domain.member.dto.request.PasswordUpdateReqBody;
import org.example.be.domain.member.dto.response.MemberDto;
import org.example.be.domain.member.dto.response.MemberProfileResBody;
import org.example.be.domain.member.entity.Member;
import org.example.be.domain.member.service.MemberService;
import org.example.be.domain.member.service.RefreshTokenStore;
import org.example.be.global.response.CommonResponse;
import org.example.be.global.security.config.SecurityUser;
import org.example.be.global.util.CookieHelper;
import org.springframework.http.HttpStatus;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;
	private final CookieHelper cookieHelper;
	private final RefreshTokenStore refreshTokenStore;

	@GetMapping("/me")
	public ResponseEntity<CommonResponse<MemberDto>> me(@AuthenticationPrincipal SecurityUser user) {
		Member member = memberService.getById(user.getId());
		boolean shouldChangePassword = memberService.isPasswordExpired(member);
		MemberDto memberDto = MemberDto.from(member, shouldChangePassword);
		return ResponseEntity.ok(CommonResponse.success(memberDto, "회원 프로필 조회 성공"));
	}

	// 비밀번호 재설정(찾기 기능 x, 로그인한 상태에서만 변경 가능, 마이페이지에서 가능)
	@PutMapping("/me/password")
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

	//타인 회원의 이름과 프로필 이미지 조회 (일단 구현되어 있어서 만들었습니답)
	@GetMapping("/{memberId}/profile")
	public ResponseEntity<CommonResponse<MemberProfileResBody>> getNameAndImage(@PathVariable Long memberId) {
		MemberProfileResBody resBody = memberService.getMemberProfile(memberId);
		return ResponseEntity.ok(CommonResponse.success(resBody, "회원 이름과 프로필 이미지 조회 성공"));
	}
}