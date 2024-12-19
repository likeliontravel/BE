package org.example.be.oauth.controller;


import org.example.be.oauth.dto.GoogleResponse;
import org.example.be.oauth.service.GoogleOAuth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스는 REST API의 컨트롤러로 동작하며, HTTP 요청을 처리합니다.
@RequestMapping("/dev/oauth2/code/google") // Google OAuth2 인증 코드 콜백 URL을 매핑합니다.
public class GoogleOauth2Controller {

    @Autowired // GoogleOAuth2Service 클래스를 의존성 주입받습니다.
    private GoogleOAuth2Service googleOAuth2Service;

    /**
     * Google OAuth2 인증 후 받은 인증 코드로 사용자 정보를 가져오는 메서드
     * @param code: Google에서 인증 후 리디렉션 URI에 포함된 인증 코드
     * @return ResponseEntity: 응답 결과를 HTTP 상태 코드와 함께 반환
     */
    @GetMapping // HTTP GET 요청을 처리하는 메서드입니다.
    public ResponseEntity<?> googleCallback(@RequestParam("code") String code) {
        try {
            // 인증 코드로 Google 사용자 정보를 가져옵니다.
            GoogleResponse userResponse = googleOAuth2Service.getUserInfo(code);

            // Google 사용자 정보를 콘솔에 출력 (실제 서비스에서는 로깅 처리 등을 통해 사용할 수 있음)
            System.out.println("code: " + code);

            // 사용자 정보를 포함한 응답을 클라이언트로 반환합니다.
            return ResponseEntity.ok(userResponse); // 200 OK 응답과 함께 사용자 정보를 반환
        } catch (Exception e) {
            // 예외가 발생하면 서버 오류 응답을 반환합니다.
            return ResponseEntity.internalServerError().body("로그인 처리 중 오류 발생");
        }
    }
}