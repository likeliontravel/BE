package org.example.be.group.invitation.controller;

import org.example.be.group.invitation.dto.InvitationResBody;
import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.resolver.DecodedPathVariable;
import org.example.be.response.CommonResponse;
import org.example.be.security.config.SecurityUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
// 그룹 초대 링크 보기 및 생성에 관련한 컨트롤러
// 그룹 초대 로직 변경 -> 조회 / 강제 생성 구분, 처음 링크 발급시 조회에서 링크가 자동생성되어 반환되며, 이미 유효한 링크가 있는데 생성하려는 경우 강제 생성 API를 사용함.
public class GroupInvitationController {

	private final GroupInvitationService invitationService;

	@Value("${app.base-url}")
	private String baseUrl;

	// 초대 링크 조회하기
	@GetMapping("/{groupName}/invitation")
	public ResponseEntity<CommonResponse<InvitationResBody>> getInvitation(
		@DecodedPathVariable("groupName") String groupName,
		@AuthenticationPrincipal SecurityUser user) {
		GroupInvitation invitation = invitationService.getValidOrExpireInvitation(groupName, user.getId());

		InvitationResBody response = new InvitationResBody(
			invitation.getInvitationCode(),
			baseUrl + "/invite/" + invitation.getInvitationCode(),
			invitation.getExpiresAt()
		);

		return ResponseEntity.ok(CommonResponse.success(response, "초대 링크 조회 성공"));
	}

	// 강제로 새로운 초대 링크 발급 ( 기존 링크 무효화 )
	@PostMapping("/{groupName}/invitation/generateNew")
	public ResponseEntity<CommonResponse<InvitationResBody>> generateNewInvitation(
		@DecodedPathVariable("groupName") String groupName,
		@AuthenticationPrincipal SecurityUser user) {
		GroupInvitation invitation = invitationService.forceGenerateNewInvitation(groupName, user.getId());

		InvitationResBody response = new InvitationResBody(
			invitation.getInvitationCode(),
			baseUrl + "/invite/" + invitation.getInvitationCode(),
			invitation.getExpiresAt()
		);

		return ResponseEntity.ok(CommonResponse.success(response, "새 초대 링크 생성 성공"));
	}

}
