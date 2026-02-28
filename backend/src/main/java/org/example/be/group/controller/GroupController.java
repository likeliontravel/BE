package org.example.be.group.controller;

import java.util.List;

import org.example.be.group.dto.GroupAddMemberResBody;
import org.example.be.group.dto.GroupCreateReqBody;
import org.example.be.group.dto.GroupDeleteResBody;
import org.example.be.group.dto.GroupDetailResBody;
import org.example.be.group.dto.GroupExitOrDeleteReqBody;
import org.example.be.group.dto.GroupExitResBody;
import org.example.be.group.dto.GroupModifyReqBody;
import org.example.be.group.dto.GroupModifyResBody;
import org.example.be.group.dto.GroupResBody;
import org.example.be.group.service.GroupService;
import org.example.be.resolver.DecodedPathVariable;
import org.example.be.response.CommonResponse;
import org.example.be.security.config.SecurityUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

	private final GroupService groupService;

	// 그룹 생성하기
	@PostMapping("/create")
	public ResponseEntity<CommonResponse<GroupResBody>> createGroup(
		@Valid @RequestBody GroupCreateReqBody request,
		@AuthenticationPrincipal SecurityUser user) {
		GroupResBody groupResponse = groupService.createGroup(request, user.getId());
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(groupResponse, "그룹 생성 성공"));
	}

	// 그룹 상세 조회
	@GetMapping("/{groupName}/detail")
	public ResponseEntity<CommonResponse<GroupDetailResBody>> getGroupDetail(
		@DecodedPathVariable("groupName") String groupName,
		@AuthenticationPrincipal SecurityUser user) {
		GroupDetailResBody groupDetail = groupService.getGroupDetail(groupName, user.getId());
		return ResponseEntity.ok(CommonResponse.success(groupDetail, "그룹 상세 정보 조회 성공"));
	}

	// (임시) 그룹에 멤버 추가 -> 그룹 초대링크 구현 후 수정 또는 삭제 예정
	@PostMapping("/addMember")
	public ResponseEntity<CommonResponse<GroupAddMemberResBody>> addMember(
		@RequestParam String groupName,
		@AuthenticationPrincipal SecurityUser user) {
		GroupAddMemberResBody response = groupService.addMemberToGroup(groupName, user.getId());

		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(response, "그룹에 멤버 추가 완료."));
	}

	// 그룹 설명 또는 공지사항 변경
	@PostMapping("/modify/description")
	public ResponseEntity<CommonResponse<GroupModifyResBody>> modifyDescription(
		@Valid @RequestBody GroupModifyReqBody request,
		@AuthenticationPrincipal SecurityUser user) {
		GroupModifyResBody response = groupService.modifyDescribtion(request, user.getId());
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(response, "그룹 설명 변경 완료"));
	}

	// 그룹 나가기
	@PostMapping("/exit")
	public ResponseEntity<CommonResponse<GroupExitResBody>> exitFromGroup(
		@Valid @RequestBody GroupExitOrDeleteReqBody request,
		@AuthenticationPrincipal SecurityUser user) {
		GroupExitResBody response = groupService.exitGroup(request, user.getId());
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(response, "그룹 나가기 완료"));
	}

	// GET 엔드포인트 추가: 로그인한 사용자가 가입한 그룹 정보 조회
	@GetMapping("/user-groups")
	public ResponseEntity<CommonResponse<List<GroupResBody>>> getUserGroups(
		@AuthenticationPrincipal SecurityUser user) {
		List<GroupResBody> groups = groupService.getAllGroups(user.getId());
		return ResponseEntity.ok(CommonResponse.success(groups, "그룹 정보 조회 성공"));
	}

	// 그룹 제거하기(삭제하기)
	@DeleteMapping("/delete")
	public ResponseEntity<CommonResponse<GroupDeleteResBody>> deleteGroup(
		@Valid @RequestBody GroupExitOrDeleteReqBody request,
		@AuthenticationPrincipal SecurityUser user) {
		GroupDeleteResBody response = groupService.deleteGroup(request, user.getId());
		return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(response, "그룹 삭제 완료"));
	}

}
