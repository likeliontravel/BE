package org.example.be.oauth.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.group.dto.GroupAddMemberRequestDTO;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.jwt.provider.JWTProvider;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTProvider jwtProvider;
    private final GroupInvitationService groupInvitationService;
    private final GroupService groupService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CommonResponse<String> commonResponse = new CommonResponse<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println("OAuth2 로그인 성공!");

            //OAuth2User
            CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
            System.out.println("인증된 사용자 : " + customUserDetails);

            String userIdentifier = customUserDetails.getUserIdentifier();
            System.out.println("userIdentifier: " + userIdentifier);

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();
            String role = auth.getAuthority();

            String accessToken = jwtProvider.generateAccessToken(userIdentifier, role);
            String refreshToken = jwtProvider.generateRefreshToken(userIdentifier, role);

            System.out.println("생성된 accessToken 토큰 : " + accessToken);
            System.out.println("생성된 refreshToken 토큰 : " + refreshToken);

            // 쿠키에 저장
            Cookie accessTokenCookie = createCookie("Authorization", accessToken);
            Cookie refreshTokenCookie = createCookie("Refresh-Token", refreshToken);
            Cookie userIdentifierCookie = createCookie("User-Identifier", userIdentifier);

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);
            response.addCookie(userIdentifierCookie);

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

            // redirect를 백엔드에서 하고 있었음. 변경 -> JSON 성공 응답만 반환해주고 redirect를 프론트가 수행하도록 함
            response.sendRedirect("https://toleave.shop/");

        } catch (Exception e) {
            // 예외 처리: 로그인 실패 시 JSON 응답 반환
            System.out.println("OAuth2 로그인 처리 중 예외 발생: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

            commonResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            commonResponse.setSuccess(Boolean.FALSE);
            commonResponse.setMessage("OAuth2 로그인 실패: " + e.getMessage());
            commonResponse.setData(null);

            mapper.writeValue(response.getWriter(), commonResponse);
        }

    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*24*1);   // 1일 만료
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain("toleave.shop");
        cookie.setAttribute("SameSite", "None");

        return cookie;
    }
}
