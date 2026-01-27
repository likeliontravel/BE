package org.example.be.generaluser.controller;

import org.example.be.generaluser.dto.GeneralUserDTO;
import org.example.be.generaluser.service.GeneralUserService;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/general-user")
public class GeneralUserController {

	private final GeneralUserService generalUserService;
	private final GroupInvitationService groupInvitationService;
	private final GroupService groupService;

	// 회원가입
	// ---- 2025.02.03 변경 ----
	// 회원가입 시 초대 코드가 전달된다면 자동으로 그룹 가입 처리
	@PostMapping("/signup")
	public ResponseEntity<CommonResponse<String>> signUp(@RequestBody GeneralUserDTO generalUserDTO
		, @RequestParam(value = "invitationCode", required = false) String invitationCode) {

		generalUserService.signUpWithInvitation(generalUserDTO, invitationCode);
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "회원 가입 성공"));
	}

	// 회원 프로필 조회 - 실서비스 미사용
	@GetMapping("/profile")
	public ResponseEntity<CommonResponse<GeneralUserDTO>> getProfile(Authentication authentication) {
		// 현재 로그인된 사용자 이메일 가져오기
		String email = authentication.getName();

		// 회원 정보를 조회
		GeneralUserDTO userProfile = generalUserService.getProfile(email);

		return ResponseEntity.ok(CommonResponse.success(userProfile, "회원 프로필 조회 성공"));
	}
}