// package org.example.be.security.handler;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.example.be.response.CommonResponse;
// import org.springframework.http.MediaType;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.security.web.authentication.AuthenticationFailureHandler;
// import org.springframework.stereotype.Component;
//
// import java.io.IOException;
//
// /*
//  * 인증 실패시 실행할 실패 핸들러 */
// @Component
// public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {
//
//     @Override
//     public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
//
//         ObjectMapper mapper = new ObjectMapper();
//
//         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//         response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8");
//
//         CommonResponse<String> commonResponse = new CommonResponse<>();
//
//         // 비밀번호 틀릴 때
//         if (exception instanceof BadCredentialsException) {
//
//             commonResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             commonResponse.setMessage(exception.getMessage());
//
//             // 유저를 찾을 수 없을 때
//         } else if (exception instanceof UsernameNotFoundException) {
//
//             commonResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             commonResponse.setMessage(exception.getMessage());
//
//             // 그 외 예외 메세지들
//         } else {
//
//             commonResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             commonResponse.setMessage(exception.getMessage());
//         }
//
//         mapper.writeValue(response.getWriter(), commonResponse);
//     }
// }
