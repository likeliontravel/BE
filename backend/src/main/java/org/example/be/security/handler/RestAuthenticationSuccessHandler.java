// package org.example.be.security.handler;
//
// import java.io.IOException;
//
// import org.example.be.generaluser.dto.GeneralUserDTO;
// import org.example.be.group.invitation.service.GroupInvitationService;
// import org.example.be.group.service.GroupService;
// import org.example.be.jwt.util.JWTUtil;
// import org.example.be.member.repository.MemberRepository;
// import org.example.be.response.CommonResponse;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContext;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.web.WebAttributes;
// import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
// import org.springframework.stereotype.Component;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
//
// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.HttpSession;
// import lombok.RequiredArgsConstructor;
//
// /*
//  * 인증 성공시 실행할 성공 핸들러 */
// @Component
// @RequiredArgsConstructor
// public class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
//
// 	private final JWTUtil jwtUtil;
// 	private final GroupService groupService;
// 	private final GroupInvitationService groupInvitationService;
// 	private final MemberRepository memberRepository;
//
// 	@Override
// 	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
// 		Authentication authentication) throws IOException {
//
// 		ObjectMapper mapper = new ObjectMapper();
//
// 		CommonResponse<GeneralUserDTO> commonResponse = new CommonResponse<>();
//
// 		// 인증된 사용자 정보 가져오기
// 		GeneralUserDTO generalUserDTO = (GeneralUserDTO)authentication.getPrincipal();
//
// 		String generalUserIdentifier = "gen" + "_" + generalUserDTO.getEmail();
//
// 		// Access 토큰 및 Refresh 토큰 생성
// 		String accessToken = jwtUtil.createJwt(generalUserIdentifier, generalUserDTO.getRole(),
// 			1000L * 60 * 60); // 1시간 유효(1000L * 60 * 60), 2분(1000L * 60 * 2)
// 		String refreshToken = jwtUtil.createJwt(generalUserIdentifier, generalUserDTO.getRole(),
// 			1000L * 60 * 60 * 24 * 7); // 7일(1000L * 60 * 60 * 24 * 7), 5분(1000L * 60 * 5)
//
// 		// Access 토큰을 쿠키에 추가
// 		Cookie accessTokenCookie = new Cookie("Authorization", accessToken);
// 		accessTokenCookie.setHttpOnly(true);
// 		accessTokenCookie.setSecure(true); // HTTPS 환경에서만 사용
// 		accessTokenCookie.setPath("/"); // 모든 경로에서 쿠키 사용 가능
// 		accessTokenCookie.setMaxAge(60 * 60 * 24 * 7); // 쿠키 유효기간 7일
// 		response.addCookie(accessTokenCookie);
//
// 		// Refresh 토큰을 쿠키에 추가
// 		Cookie refreshTokenCookie = new Cookie("Refresh-Token", refreshToken);
// 		refreshTokenCookie.setHttpOnly(true);
// 		refreshTokenCookie.setSecure(true); // HTTPS 환경에서만 사용
// 		refreshTokenCookie.setPath("/"); // 모든 경로에서 쿠키 사용 가능
// 		refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7); // 쿠키 유효기간 7일
// 		response.addCookie(refreshTokenCookie);
//
// 		// Access 토큰을 헤더에 추가
// 		response.addHeader("Authorization", "Bearer " + accessToken);
// 		// Refresh 토큰을 HTTP 응답에 포함 (로컬 스토리지 저장용)
// 		response.addHeader("Refresh-Token", "Bearer " + refreshToken);
//
// 		// URL 쿼리 파라미터에 invitationCode 확인. 만약 있다면 로그인과 동시에 해당 그룹에 자동 멤버 추가
// 		String invitationCode = request.getParameter("invitationCode");
// 		if (invitationCode != null && !invitationCode.isEmpty()) {
// 			try {
// 				var invitation = groupInvitationService.getValidInvitation(invitationCode);
// 				// TODO: GeneralUser -> Member 시스템 통합 완료 시 ifPresent -> orElseThrow로 변경 OR 레거시 폼로그인 필터 및 핸들러 전부 삭제
// 				memberRepository.findByEmail(generalUserDTO.getEmail())
// 					.ifPresent(
// 						member -> groupService.addMemberToGroup(invitation.getGroup().getGroupName(), member.getId()));
// 			} catch (Exception e) {
// 				System.out.println("로그인 후 자동 그룹 가입 실패: " + e.getMessage());
// 				throw new IllegalArgumentException("초대 코드가 유효하지 않습니다. " + e.getMessage());
// 			}
// 		}
//
// 		// SecurityContext에 인증 정보 저장하기
// 		SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
// 		context.setAuthentication(authentication);
// 		SecurityContextHolder.setContext(context);
//
// 		response.setStatus(HttpStatus.OK.value());
// 		response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
//
// 		// 비밀번호 제거 (보안상 제거)
// 		generalUserDTO.setPassword(null);
//
// 		commonResponse.setStatus(HttpStatus.OK.value());
// 		commonResponse.setSuccess(Boolean.TRUE);
// 		commonResponse.setMessage("로그인 성공");
// 		commonResponse.setData(generalUserDTO);
//
// 		// 응답 본문에 사용자 정보 및 메시지 작성
// 		mapper.writeValue(response.getWriter(), commonResponse);
//
// 		// 인증 예외 정보 제거
// 		clearAuthenticationAttributes(request);
// 	}
//
// 	private void clearAuthenticationAttributes(HttpServletRequest request) {
//
// 		HttpSession session = request.getSession(false);
//
// 		if (session == null) {
//
// 			return;
// 		}
//
// 		session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
// 	}
// }
