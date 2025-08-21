package org.example.be.oauth.handler;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.group.dto.GroupAddMemberRequestDTO;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final GroupInvitationService groupInvitationService;
    private final GroupService groupService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String userIdentifier = customUserDetails.getUserIdentifier();
        String role = customUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("ROLE_USER");

        // AccessToken, RefreshToken 발급
        String accessToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60); //1시간(1000L * 60 * 60), 2분(1000L * 60 * 2)
        String refreshToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60 * 24 * 7); // 7일(1000L * 60 * 60 * 24 * 7)

        System.out.println("로그인 성공: " + userIdentifier);
        System.out.println("생성된 accessToken 토큰 : " + accessToken);
        System.out.println("생성된 refreshToken 토큰 : " + refreshToken);

        // 쿠키, 헤더 각각 추가
        response.addCookie(createCookie("Authorization", accessToken));
        response.addCookie(createCookie("Refresh-Token", refreshToken));
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh-Token", "Bearer " + refreshToken);

        // OAuth2 로그인 성공 후 URL 쿼리 파라미터에서  invitationCode가 있는지 확인
        String invitationCode = request.getParameter("invitationCode");
        if (invitationCode != null && !invitationCode.isEmpty()) {  // invitationCode가 파라미터에 포함되어 있었다면 자동으로 해당 그룹에 멤버 추가
            try {
                var invitation = groupInvitationService.getValidInvitation(invitationCode);
                GroupAddMemberRequestDTO dto = new GroupAddMemberRequestDTO();
                dto.setGroupName(invitation.getGroup().getGroupName());
                dto.setUserIdentifier(userIdentifier);
                groupService.addMemberToGroup(dto);
            } catch (Exception e) {
                System.out.println("OAuth2 로그인 후 자동 그룹 가입 실패 : " + e.getMessage());
                throw new IllegalArgumentException("초대 코드가 유효하지 않습니다. " + e.getMessage());
            }
        }

        // SecurityContext에 인증 정보 저장하기
        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        response.sendRedirect("https://toleave.shop/");
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 24 * 7);
        cookie.setSecure(true); // https 적용 시 주석 해제할 것
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        //System.out.println("Cookie: " + cookie.getValue());

        return cookie;
    }
}