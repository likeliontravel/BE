package org.example.be.oauth.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.generaluser.dto.GeneralUserDTO;
import org.example.be.group.dto.GroupAddMemberRequestDTO;
import org.example.be.group.invitation.service.GroupInvitationService;
import org.example.be.group.service.GroupService;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.example.be.oauth.dto.SocialUserDTO;
import org.example.be.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
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

    // 쿠키 스펙
    private static final String COOKIE_DOMAIN = "toleave.shop";
    private static final String COOKIE_PATH = "/";
    private static final int ACCESS_COOKIE_MAX_AGE = 60 * 60 ; // != jwt token age
    private static final int REFRESH_COOKIE_MAX_AGE = 60 * 60 * 24 * 7; // != jwt token age

    // 쿠키 이름
    private static final String ACCESS_COOKIE_NAME = "Authorization";
    private static final String REFRESH_COOKIE_NAME = "Refresh-Token";

    // 최종 리다이렉트 목적지 -> 리다이렉트 미사용으로 임시 비활성화
    private static final String FE_REDIRECT_URL = "https://toleave.shop/oauth2/redirect";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

//        // 3차 수정 : 백엔드 리다이렉트 방식 대신 JSON 응답만 보내는 방식 적용
//        ObjectMapper mapper = new ObjectMapper();
//        CommonResponse<SocialUserDTO> commonResponse = new CommonResponse<>();
//
//        // 사용자 정보
//        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
//        String userIdentifier = oAuth2User.getUserIdentifier();
//        String name = oAuth2User.getName();
//        String email = oAuth2User.getEmail();
//        String provider = oAuth2User.getSocialUser().getProvider();
//        String role = oAuth2User.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .findFirst()
//                .orElse("ROLE_USER");
//
//        // 토큰 생성
//        String accessToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60); // 1시간
//        String refreshToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60 * 24 * 7); // 7일
//
//        // 쿠키 설정
//        addJwtCookie(response, ACCESS_COOKIE_NAME, accessToken, ACCESS_COOKIE_MAX_AGE);
//        addJwtCookie(response, REFRESH_COOKIE_NAME, refreshToken, REFRESH_COOKIE_MAX_AGE);
//
//        // 헤더 추가
//        response.addHeader("Authorization", "Bearer " + accessToken);
//        response.addHeader("Refresh-Token", "Bearer " + refreshToken);
//
//        // 초대 코드 포함 시 자동 그룹 가입
//        String invitationCode = request.getParameter("invitationCode");
//        if (invitationCode != null && !invitationCode.isEmpty()) {
//            try {
//                var invitation = groupInvitationService.getValidInvitation(invitationCode);
//                GroupAddMemberRequestDTO dto = new GroupAddMemberRequestDTO();
//                dto.setGroupName(invitation.getGroup().getGroupName());
//                dto.setUserIdentifier(userIdentifier);
//                groupService.addMemberToGroup(dto);
//            } catch (Exception e) {
//                System.out.println("소셜 로그인 후 자동 그룹 가입 실패 : " + e.getMessage());
//            }
//        }
//
//        // SecurityContext 저장
//        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
//        context.setAuthentication(authentication);
//        SecurityContextHolder.setContext(context);
//
//        // SocialUserDTO 생성 - 프론트에서는 provider필드를 이용할지 안할지 결정해서 마음대로 쓸 수 있도록 해놓기
//        SocialUserDTO dto = new SocialUserDTO();
//        dto.setUserIdentifier(userIdentifier);
//        dto.setEmail(email);
//        dto.setName(name);
//        dto.setProvider(provider);
//        dto.setRole(role);
//
//        // 응답 구성
//        commonResponse.setStatus(HttpStatus.OK.value());
//        commonResponse.setSuccess(true);
//        commonResponse.setMessage("로그인 성공");
//        commonResponse.setData(dto);
//
//        // JSON 응답 전송
//        response.setStatus(HttpStatus.OK.value());
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
//        mapper.writeValue(response.getWriter(), commonResponse);

      // 기존에서 쿠키 종류만 responseCookie로 수정했을 때의 호출부 버전
        // 사용자 권한
        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        String userIdentifier = principal.getUserIdentifier();
        String role = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        // JWT 발급 (JWTUtil 이용)
        String accessToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60);  // 액세스 토큰 유효기간 1시간
        String refreshToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60 * 24 * 7);    // 리프레시 토큰 유효기간 7일

        // 쿠키 설정
        addJwtCookie(response, ACCESS_COOKIE_NAME, accessToken, ACCESS_COOKIE_MAX_AGE);
        addJwtCookie(response, REFRESH_COOKIE_NAME, refreshToken, REFRESH_COOKIE_MAX_AGE);

        // 헤더에 추가 (혹시 몰라 추가. 쿠키 인증 문제 없을 시 헤더 인증방식 제거 검토)
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh-Token", "Bearer " + refreshToken);

        // 초대 코드가 있을 시 자동 그룹 가입 처리
        String invitationCode = request.getParameter("invitationCode");
        if (invitationCode != null && !invitationCode.isEmpty()) {
            try {
                var invitation = groupInvitationService.getValidInvitation(invitationCode);
                GroupAddMemberRequestDTO dto = new GroupAddMemberRequestDTO();
                dto.setGroupName(invitation.getGroup().getGroupName());
                dto.setUserIdentifier(userIdentifier);
                groupService.addMemberToGroup(dto);
            } catch (Exception e) {
                // 초대 코드가 유효하지 않아도 로그인 자체는 성공이므로, 여기서 전체 플로우를 깨지 않도록 예외 메시지 로깅만 해준다.
                System.out.println("OAuth2 로그인 후 자동 그룹 가입 실패 : " + e.getMessage());
            }
        }

        // SecurityContext 저장
        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 최종 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, FE_REDIRECT_URL);

        // ResponseCookie로 전환하기 이전 코드. 테스트 이후 문제 없을 시 삭제 예정
//        //OAuth2User
//        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
//        String userIdentifier = customUserDetails.getUserIdentifier();
//        String role = customUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("ROLE_USER");
//
//        // AccessToken, RefreshToken 발급
//        String accessToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60); //1시간(1000L * 60 * 60), 2분(1000L * 60 * 2)
//        String refreshToken = jwtUtil.createJwt(userIdentifier, role, 1000L * 60 * 60 * 24 * 7); // 7일(1000L * 60 * 60 * 24 * 7)
//
//        System.out.println("로그인 성공: " + userIdentifier);
//        System.out.println("생성된 accessToken 토큰 : " + accessToken);
//        System.out.println("생성된 refreshToken 토큰 : " + refreshToken);
//
//        // 쿠키, 헤더 각각 추가
//        response.addCookie(createCookie("Authorization", accessToken));
//        response.addCookie(createCookie("Refresh-Token", refreshToken));
//        response.setHeader("Authorization", "Bearer " + accessToken);
//        response.setHeader("Refresh-Token", "Bearer " + refreshToken);
//
//        // OAuth2 로그인 성공 후 URL 쿼리 파라미터에서  invitationCode가 있는지 확인
//        String invitationCode = request.getParameter("invitationCode");
//        if (invitationCode != null && !invitationCode.isEmpty()) {  // invitationCode가 파라미터에 포함되어 있었다면 자동으로 해당 그룹에 멤버 추가
//            try {
//                var invitation = groupInvitationService.getValidInvitation(invitationCode);
//                GroupAddMemberRequestDTO dto = new GroupAddMemberRequestDTO();
//                dto.setGroupName(invitation.getGroup().getGroupName());
//                dto.setUserIdentifier(userIdentifier);
//                groupService.addMemberToGroup(dto);
//            } catch (Exception e) {
//                System.out.println("OAuth2 로그인 후 자동 그룹 가입 실패 : " + e.getMessage());
//                throw new IllegalArgumentException("초대 코드가 유효하지 않습니다. " + e.getMessage());
//            }
//        }
//
//        // SecurityContext에 인증 정보 저장하기
//        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
//        context.setAuthentication(authentication);
//        SecurityContextHolder.setContext(context);
//
//        response.sendRedirect("https://toleave.shop/");
    }

    // JWTUtil.createCookie 사용으로 통일하기로 결정. 문제 생길 시 복구 예정
//    private Cookie createCookie(String key, String value) {
//        Cookie cookie = new Cookie(key, value);
//        cookie.setMaxAge(60 * 60 * 24 * 7);
//        cookie.setSecure(true); // https 적용 시 주석 해제할 것
//        cookie.setPath("/");
//        cookie.setHttpOnly(true);
//
//        //System.out.println("Cookie: " + cookie.getValue());
//
//        return cookie;
//    }

    // 소셜로그인 성공 핸들러에서만 사용할 쿠키 세팅 메서드. ResponseCookie를 사용하도록 변경
    private void addJwtCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .domain(COOKIE_DOMAIN)
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)  // ResponseCookie에서는 초 단위 사용
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }



}