package org.example.be.group.controller;

import lombok.RequiredArgsConstructor;
import org.example.be.group.dto.*;
import org.example.be.group.service.GroupService;
import org.example.be.resolver.DecodedPathVariable;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    // 그룹 생성하기
    @PostMapping("/create")
    public ResponseEntity<CommonResponse<GroupResponseDTO>> createGroup(@RequestBody GroupCreationRequestDTO request) {
        GroupResponseDTO groupResponse = groupService.createGroup(request);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(groupResponse, "그룹 생성 성공"));
    }

    // 그룹 상세 조회
    @GetMapping("/{groupName}/detail")
    public ResponseEntity<CommonResponse<GroupDetailDTO>> getGroupDetail(@DecodedPathVariable String groupName) {
        GroupDetailDTO groupDetail = groupService.getGroupDetail(groupName);
        return ResponseEntity.ok(CommonResponse.success(groupDetail, "그룹 상세 정보 조회 성공"));
    }

    // (임시) 그룹에 멤버 추가 -> 그룹 초대링크 구현 후 수정 또는 삭제 예정
    @PostMapping("/addMember")
    public ResponseEntity<CommonResponse<String>> addMember(@RequestBody GroupAddMemberRequestDTO request) {
        groupService.addMemberToGroup(request);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "그룹에 멤버 추가 완료."));
    }

    // 그룹 설명 또는 공지사항 변경
    @PostMapping("/modify/description")
    public ResponseEntity<CommonResponse<String>> modifyDescription(@RequestBody GroupModifyRequestDTO request) {
        groupService.modifyDescribtion(request);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "그룹 설명 변경 완료"));
    }

    // 그룹 나가기
    @PostMapping("/exit")
    public ResponseEntity<CommonResponse<String>> exitFromGroup(@RequestBody GroupExitOrDeleteRequestDTO request) {
            groupService.exitGroup(request);

            return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "그룹 나가기 완료"));
    }

    // 사용자가 가입한 그룹 조회하기 // 임시 주석 처리 - 테스트
//    @PostMapping("/user-groups")
//    public ResponseEntity<CommonResponse<List<GroupResponseDTO>>> getUserGroups(@RequestParam String userIdentifier) {
//        List<GroupResponseDTO> groups = groupService.getAllGroups(userIdentifier);
//
//        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(groups, "사용자가 가입한 그룹 가져오기 완료"));
//    }

    // GET 엔드포인트 추가: 로그인한 사용자가 가입한 그룹 정보 조회
    @GetMapping("/user-groups")
    public ResponseEntity<CommonResponse<List<GroupResponseDTO>>> getUserGroups() {
        List<GroupResponseDTO> groups = groupService.getAllGroups();
        return ResponseEntity.ok(CommonResponse.success(groups, "그룹 정보 조회 성공"));
    }

    // 그룹 제거하기(삭제하기)
    @DeleteMapping("/delete")
    public ResponseEntity<CommonResponse<String>> deleteGroup(@RequestBody GroupExitOrDeleteRequestDTO request) {
        groupService.deleteGroup(request);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success(null, "그룹 삭제 완료"));
    }

}
