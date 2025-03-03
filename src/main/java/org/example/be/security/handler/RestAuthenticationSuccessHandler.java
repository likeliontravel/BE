package org.example.be.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.be.group.dto.GroupAddMemberRequestDTO;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.jwt.provider.JWTProvider;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.response.CommonResponse;
import org.example.be.generaluser.dto.GeneralUserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
 * 인증 성공시 실행할 성공 핸들러 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTProvider jwtProvider;
    private final GroupInvitationService groupInvitationService;
    private final GroupService groupService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        CommonResponse<GeneralUserDTO> commonResponse = new CommonResponse<>();

        // 인증된 사용자 정보 가져오기
        GeneralUserDTO generalUserDTO = (GeneralUserDTO) authentication.getPrincipal();

        String generalUserIdentifier = "gen" + " " + generalUserDTO.getEmail();

        // Access 토큰 및 Refresh 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(generalUserIdentifier, generalUserDTO.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(generalUserIdentifier, generalUserDTO.getRole());

        // 쿠키에 AccessToken, RefreshToken, UserIdentifier 저장
        Cookie accessTokenCookie = createCookie("Authorization", accessToken);
        Cookie refreshTokenCookie = createCookie("Refresh-Token", refreshToken);
        Cookie userIdentifierCookie = createCookie("User-Identifier", generalUserIdentifier);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
        response.addCookie(userIdentifierCookie);

        // URL 쿼리 파라미터에 invitationCode 확인. 만약 있다면 로그인과 동시에 해당 그룹에 자동 멤버 추가
        String invitationCode = request.getParameter("invitationCode");
        if (invitationCode != null && !invitationCode.isEmpty()) {
            try {
                var invitation = groupInvitationService.getValidInvitation(invitationCode);
                GroupAddMemberRequestDTO dto = new GroupAddMemberRequestDTO();
                dto.setGroupName(invitation.getGroup().getGroupName());
                dto.setUserIdentifier(authentication.getName());
                groupService.addMemberToGroup(dto);
            } catch (Exception e) {
                System.out.println("로그인 후 자동 그룹 가입 실패: " + e.getMessage());
            }
        }

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        // 비밀번호 제거 (보안상 제거)
        generalUserDTO.setPassword(null);

        commonResponse.setStatus(HttpStatus.OK.value());
        commonResponse.setSuccess(Boolean.TRUE);
        commonResponse.setMessage("로그인 성공");
        commonResponse.setData(generalUserDTO);

        // 응답 본문에 사용자 정보 및 메시지 작성
        mapper.writeValue(response.getWriter(), commonResponse);

        // 인증 예외 정보 제거
        clearAuthenticationAttributes(request);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {

            return;
        }

        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1일
        cookie.setDomain("toleave.shop");
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }
}
