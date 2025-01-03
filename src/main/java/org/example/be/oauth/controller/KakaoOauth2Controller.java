package org.example.be.oauth.controller;

import org.example.be.oauth.dto.KakaoResponse;
import org.example.be.oauth.service.KakaoOAuth2Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/login/oauth2/code/kakao") // Kakao OAuth2 인증 코드 콜백 URL
public class KakaoOauth2Controller {

    private final KakaoOAuth2Service kakaoOAuth2Service;

    // 카카오 인증을 위한 설정 값들을 컨트롤러에서 받아옵니다.
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    // 의존성 주입
    public KakaoOauth2Controller(KakaoOAuth2Service kakaoOAuth2Service) {
        this.kakaoOAuth2Service = kakaoOAuth2Service;
    }

    /**
     * 카카오 로그인 화면으로 리디렉션
     */
    @GetMapping("/login")
    public RedirectView kakaoLogin() {
        String loginUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;

        return new RedirectView(loginUrl);  // 카카오 로그인 화면으로 리디렉션
    }

    /**
     * 카카오 인증 코드로 사용자 정보를 가져오는 엔드포인트
     */
    @GetMapping
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code) {
        try {
            // 인증 코드로 Kakao 사용자 정보를 가져옵니다.
            KakaoResponse userResponse = kakaoOAuth2Service.getUserInfo(code);

            // 사용자 정보를 콘솔에 출력 (실제 서비스에서는 로깅 처리 등을 통해 사용할 수 있음)
            System.out.println("code: " + code);

            // 사용자 정보를 포함한 응답을 클라이언트로 반환합니다.
            return ResponseEntity.ok(userResponse); // 200 OK 응답과 함께 사용자 정보를 반환
        } catch (Exception e) {
            // 예외가 발생하면 서버 오류 응답을 반환합니다.
            return ResponseEntity.internalServerError().body("로그인 처리 중 오류 발생");
        }
    }
}
