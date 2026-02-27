package org.example.be.group.invitation.controller;

import java.io.IOException;

import org.example.be.group.invitation.entity.GroupInvitation;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.resolver.DecodedPathVariable;
import org.example.be.security.config.SecurityUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/invite")
@RequiredArgsConstructor
// 로그인 및 비로그인 상태에서 초대 링크 클릭 시 리다이렉션 또는 자동가입 처리를 위한 컨트롤러
public class InvitationJoinController {

	private final GroupInvitationService invitationService;
	private final GroupService groupService;

	// 로그인 상태에서 초대 링크 클릭 시 자동으로 그룹 가입 처리
	// 비 로그인 상태에서 초대 링크 클릭 시 로그인 페이지로 리다이렉트.
	// 단, 초대 코드를 쿠키(pendingInvitationCode)에 담아 로그인/회원가입 흐름에서
	// CustomSuccessHandler 또는 MemberController가 읽어 그룹 자동 가입에 활용
	@GetMapping("/{invitationCode}")
	public void handleInvitation(
		@DecodedPathVariable String invitationCode,
		Authentication authentication,
		@AuthenticationPrincipal SecurityUser user,
		HttpServletResponse response) throws IOException {

		try {
			// 초대코드가 유효한 상태라면 해당 초대코드를 정상적으로 다시 반환받음.
			GroupInvitation invitation = invitationService.getValidInvitation(invitationCode);

			// 로그인 여부 확인. 로그인 상태라면 바로 자동가입 처리
			if (authentication != null && authentication.isAuthenticated()) {
				String groupName = invitation.getGroup().getGroupName();
				groupService.addMemberToGroup(groupName, user.getId());

				// 그룹 가입 성공 응답
				response.setStatus(HttpStatus.OK.value());
				response.setContentType("application/json;charset=utf-8");
				response.getWriter().write("{\"success\":true, \"message\":\"그룹 가입이 완료되었습니다.\"}");
				return;
			}

			// 비로그인 상태: pendingInvitationCode 쿠키를 설정하고 로그인 페이지로 리다이렉트
			// SameSite=Lax - OAuth 리다이렉트 체인에서도 쿠키가 함께 전송됨.
			ResponseCookie pendingCookie = ResponseCookie.from("pendingInvitationCode", invitationCode)
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(600)    // 10분
				.sameSite("Lax")
				.build();
			response.addHeader(HttpHeaders.SET_COOKIE, pendingCookie.toString());

			// 로그인 페이지로 리다이렉트
			response.sendRedirect("https://toleave.cloud/login");

		} catch (Exception e) {
			// 초대코드가 유효하지 않으면 쿼리파라미터 없이 로그인페이지로 리다이렉트
			response.sendRedirect("https://toleave.cloud/login");
		}

	}

}
