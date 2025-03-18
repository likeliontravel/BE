package org.example.be.group.invitation.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.group.dto.GroupAddMemberRequestDTO;
import org.example.be.group.entitiy.Group;
import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.response.CommonResponse;
import org.example.be.security.util.SecurityUtil;
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
    // 비 로그인 상태에서 초대 링크 클릭 시 로그인 페이지로 리다이렉트.
    // 단, 초대 코드를 쿼리 파라미터에 포함하여 현재 상황을 기억하게 하고,
    // 로그인 시 성공 핸들러에서 해당 파라미터를 이용해 한번 더 리다이렉션.
    @GetMapping("/{invitationCode}")
    public void handleInvitation(
            @PathVariable String invitationCode,
            Authentication authentication,
            HttpServletResponse response) throws IOException {

        try {
            // 초대코드가 유효한 상태라면 해당 초대코드를 정상적으로 다시 반환받음.
            GroupInvitation invitation = invitationService.getValidInvitation(invitationCode);

            // 로그인 여부 확인. 로그인 상태라면 바로 자동가입 처리
            if (authentication != null && authentication.isAuthenticated()) {
                String userIdentifier = SecurityUtil.getUserIdentifierFromAuthentication();
                String groupName = invitation.getGroup().getGroupName();

                GroupAddMemberRequestDTO addMemeberRequestDTO = new GroupAddMemberRequestDTO();
                addMemeberRequestDTO.setGroupName(groupName);
                addMemeberRequestDTO.setUserIdentifier(userIdentifier);
                groupService.addMemberToGroup(addMemeberRequestDTO);

                // 그룹 가입 성공 응답
                response.setStatus(HttpStatus.OK.value());
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"success\":true, \"message\":\"그룹 가입이 완료되었습니다.\"}");
                return;
            }

            // 로그인 상태가 아니라면 초대코드를 쿼리파라미터로 추가하여 login페이지로 리다이렉트
            String redirectUrl = "https://localhost:3000/login?invitationCode=" + invitationCode;
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            // 초대코드가 유효하지 않으면 쿼리파라미터 없이 로그인페이지로 리다이렉트
            response.sendRedirect("https://localhost:3000/login");
        }

    }


}
