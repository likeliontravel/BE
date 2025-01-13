package org.example.be.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.response.CommonResponse;
import org.example.be.user.dto.UserDTO;
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

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        CommonResponse<UserDTO> commonResponse = new CommonResponse<>();

        // 인증된 사용자 정보 가져오기
        UserDTO userDTO = (UserDTO) authentication.getPrincipal();

        // Access 토큰 및 Refresh 토큰 생성
        String accessToken = jwtUtil.createJwt(userDTO.getEmail(), userDTO.getRole(), 1000L * 60 * 60); // 1시간 유효
        String refreshToken = jwtUtil.createJwt(userDTO.getEmail(), userDTO.getRole(), 1000L * 60 * 60 * 24 * 7); // 7일 유효

        // Access 토큰을 쿠키에 추가
        Cookie accessTokenCookie = new Cookie("Authorization", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true); // HTTPS 환경에서만 사용
        accessTokenCookie.setPath("/"); // 모든 경로에서 쿠키 사용 가능
        accessTokenCookie.setMaxAge(60 * 60); // 1시간 만료
        response.addCookie(accessTokenCookie);

        // Refresh 토큰을 HTTP 응답에 포함 (로컬 스토리지 저장용)
        response.addHeader("Refresh-Token", refreshToken);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        // 비밀번호 제거 (보안상 제거)
        userDTO.setPassword(null);

        commonResponse.setStatus(HttpStatus.OK.value());
        commonResponse.setSuccess(Boolean.TRUE);
        commonResponse.setMessage("로그인 성공");
        commonResponse.setData(userDTO);

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
}
