package org.example.be.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
public class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        CommonResponse<UserDTO> commonResponse = new CommonResponse<>();

        UserDTO userDTO = (UserDTO) authentication.getPrincipal();

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        userDTO.setPassword(null);

        commonResponse.setStatus(HttpStatus.OK.value());
        commonResponse.setSuccess(Boolean.TRUE);
        commonResponse.setMessage("로그인 성공");
        commonResponse.setData(userDTO);

        mapper.writeValue(response.getWriter(), commonResponse);

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
