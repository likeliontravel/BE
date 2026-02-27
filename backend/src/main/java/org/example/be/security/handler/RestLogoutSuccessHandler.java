// package org.example.be.security.handler;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;
// import org.example.be.response.CommonResponse;
// import org.springframework.http.MediaType;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
// import org.springframework.stereotype.Component;
//
// import java.io.IOException;
//
// @Component
// @RequiredArgsConstructor
// public class RestLogoutSuccessHandler implements LogoutSuccessHandler {
//
//     @Override
//     public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
//
//         Object error = request.getAttribute("logoutError");
//         response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8");
//
//         ObjectMapper mapper = new ObjectMapper();
//
//         if (error != null) {
//             // 실패 응답 감지 시 여기서 단일하게 응답 송출
//             CommonResponse<String> commonResponse = CommonResponse.error(400, String.valueOf(error));
//             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//             response.getWriter().write(mapper.writeValueAsString(commonResponse));
//             response.getWriter().flush();
//             return;
//         }
//
//         CommonResponse<String> commonResponse = CommonResponse.success(null, "성공적으로 로그아웃 되었습니다.");
//         response.setStatus(HttpServletResponse.SC_OK);
//         response.getWriter().write(mapper.writeValueAsString(commonResponse));
//         response.getWriter().flush();
// //
// //        CommonResponse<String> commonResponse = CommonResponse.success(
// //                null, "성공적으로 로그아웃 되었습니다."
// //        );
// //
// //        response.setStatus(HttpServletResponse.SC_OK);
// //        response.setContentType(MediaType.APPLICATION_JSON_VALUE+";charset=utf-8");
// //        response.getWriter().write(mapper.writeValueAsString(commonResponse));
// //
// //        response.getWriter().flush();
//     }
// }
