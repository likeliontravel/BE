package org.example.be.group.invitation.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.group.dto.GroupAddMemberRequestDTO;
import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/invite")
@RequiredArgsConstructor
// 로그인 및 비로그인 상태에서 초대 링크 클릭 시 리다이렉션 또는 자동가입 처리를 위한 컨트롤러
public class InvitationJoinController {

    private final GroupInvitationService invitationService;
    private final GroupService groupService;

    // 로그인 상태에서 초대 링크 클릭 시 자동으로 그룹 가입 처리
    @PostMapping("/{invitationCode}/join")
    public ResponseEntity<CommonResponse<String>> joinGroup(
            @PathVariable String invitationCode,
            Authentication authentication) {

        // 로그인 된 사용자이므로 초대 코드를 이용해 자동으로 해당 그룹에 가입처리
        GroupInvitation invitation = invitationService.getValidInvitation(invitationCode);
        String groupName = invitation.getGroup().getGroupName();
        String userIdentifier = authentication.getName();
        GroupAddMemberRequestDTO dto = new GroupAddMemberRequestDTO();
        dto.setGroupName(groupName);
        dto.setUserIdentifier(userIdentifier);
        groupService.addMemberToGroup(dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(null, "그룹 가입이 완료되었습니다."));
    }

    // 비 로그인 상태에서 초대 링크 클릭 시 로그인 페이지로 리다이렉트.
    // 단, 초대 코드를 쿼리 파라미터에 포함하여 현재 상황을 기억하게 하고,
    // 로그인 시 성공 핸들러에서 해당 파라미터를 이용해 한번 더 리다이렉션.
    @GetMapping("/{invitationCode}")
    public void handleInvitation(
            @PathVariable String invitationCode,
            HttpServletResponse response) throws IOException {
        try {
            invitationService.getValidInvitation(invitationCode);
        } catch (Exception E) {
            // 초대 코드가 유효하지 않으면 기본 로그인 페이지로 리다이렉트. 이 때 파라미터를 남기지 않는다.(요청 자체를 기억하지 않는다)
            response.sendRedirect("https://localhost:8080/loginPage");  // 이 부분은 프론트와 상의해야함. 로그인 페이지의 URL을 알아야합니다.
            return;
        }

        // 초대 코드가 유효하면
        // 초대 코드를 쿼리 파라미터에 포함하여 로그인 페이지로 리다이렉트
        String redirectUrl = "https://localhost:8080/loginPage?invitationCode=" + invitationCode;
        response.sendRedirect(redirectUrl);
    }


}
