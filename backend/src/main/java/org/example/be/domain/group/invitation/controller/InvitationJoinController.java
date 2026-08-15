package org.example.be.domain.group.invitation.controller;

import java.io.IOException;

import org.example.be.domain.group.invitation.entity.GroupInvitation;
import org.example.be.domain.group.invitation.service.GroupInvitationService;
import org.example.be.domain.group.invitation.util.InviteRedirectHelper;
import org.example.be.domain.group.service.GroupService;
import org.example.be.global.exception.BusinessException;
import org.example.be.global.exception.code.ErrorCode;
import org.example.be.global.security.config.SecurityUser;
import org.example.be.global.util.DecodedPathVariable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/invite")
@RequiredArgsConstructor
// 로그인 및 비로그인 상태에서 초대 링크 클릭 시 리다이렉션 또는 자동가입 처리를 위한 컨트롤러
public class InvitationJoinController {

	private final GroupInvitationService invitationService;
	private final GroupService groupService;
	private final InviteRedirectHelper inviteRedirectHelper;

	// 초대 링크는 브라우저 주소창으로 진입하므로 모든 분기가 프론트엔드로의 리다이렉트(302)로 끝나야 한다.
	// JSON을 반환하면 사용자가 백엔드 도메인에 머문 채 응답 본문을 그대로 보게 된다.
	// 로그인 상태: 자동으로 그룹 가입 처리 후 그룹 상세페이지로 리다이렉트
	// 비로그인 상태: 초대 코드를 쿠키(pendingInvitationCode)에 담고 로그인 페이지로 리다이렉트.
	//             이후 로그인 / 회원가입 흐름에서 CustomSuccessHandler 또는 AuthController가
	//             해당 쿠키를 읽어 그룹 자동 가입에 활용한다.
	@GetMapping("/{invitationCode}")
	public void handleInvitation(
		@DecodedPathVariable String invitationCode,
		@AuthenticationPrincipal SecurityUser user,
		HttpServletResponse response) throws IOException {

		// 이미 멤버인 경우에도 그룹 페이지로 보내야 하므로 catch 블록에서 참조할 수 있게 try 밖에 선언
		Long groupId = null;

		try {
			GroupInvitation invitation = invitationService.getValidInvitation(invitationCode);
			groupId = invitation.getGroup().getId();

			// 로그인 여부는 반드시 user의 null 여부로 판단해야 한다.
			// CustomAuthenticationFilter가 비로그인 요청에도 AnonymousAuthenticationToken을 넣는데
			// 이 토큰의 isAuthenticated()는 true를 반환하므로 인증 객체만으로는 로그인 여부를 구분할 수 없다.
			// 익명일 때 principal은 문자열 "anonymousUser"라 SecurityUser타입이 아니므로 user는 null이 된다.
			if (user == null) {
				// 비로그인 상태: 초대코드를 쿠키에 담아 로그인 이후 자동가입에 사용
				// SameSite=Lax - OAuth 리다이렉트 체인에서도 쿠키가 함께 전송됨.
				ResponseCookie pendingCookie = ResponseCookie.from("pendingInvitationCode", invitationCode)
					.httpOnly(true)
					.secure(true)
					.path("/")
					.maxAge(600)    // 10분
					.sameSite("Lax")
					.build();
				response.addHeader(HttpHeaders.SET_COOKIE, pendingCookie.toString());

				response.sendRedirect(inviteRedirectHelper.loginUrl());
				return;
			}

			// 로그인 상태: 자동 가입 처리 후 그룹 상세 페이지로
			groupService.addMemberToGroup(invitation.getGroup().getGroupName(), user.getId());
			response.sendRedirect(
				inviteRedirectHelper.groupPageUrl(groupId, InviteRedirectHelper.JOINED_NEW));

		} catch (BusinessException e) {
			// 이미 그룹에 속한 사용자가 링크를 다시 클릭한 경우.
			// 사용자 입장에서는 "그룹에 속해 있다"는 결과가 같으므로 실패가 아닌 성공으로 취급한다.
			if (e.getErrorCode() == ErrorCode.GROUP_ALREADY_MEMBER && groupId != null) {
				log.info("[invite] 이미 그룹 멤버 - invitationCode={}, groupId={}", invitationCode, groupId);
				response.sendRedirect(
					inviteRedirectHelper.groupPageUrl(groupId, InviteRedirectHelper.JOINED_ALREADY));
				return;
			}

			// 만료 / 무효 코드 등 예상 가능한 실패
			log.warn("[invite] 초대 처리 실패 - invitationCode={}, errorCode={}, message={}",
				invitationCode, e.getErrorCode(), e.getMessage());
			response.sendRedirect(inviteRedirectHelper.inviteErrorUrl(e.getErrorCode().name()));
		} catch (Exception e) {
			// 예상하지 못한 예외만 error 레벨로 남긴다
			log.error("[invite] 초대 처리 중 예기치 못한 오류 - invitationCode={}", invitationCode, e);
			response.sendRedirect(
				inviteRedirectHelper.inviteErrorUrl(ErrorCode.INTERNAL_SERVER_ERROR.name())
			);
		}
	}
}