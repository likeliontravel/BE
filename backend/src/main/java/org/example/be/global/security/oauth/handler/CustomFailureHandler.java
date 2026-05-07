package org.example.be.global.security.oauth.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

	// 에러코드를 식별할 수 없을 때 사용하는 기본 코드 (catch-all)
	private static final String DEFAULT_ERROR_CODE = "oauth_failed";

	@Value("${app.frontend-base-url}")
	private String frontendBaseUrl;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception)
		throws IOException, ServletException {

		String errorCode = DEFAULT_ERROR_CODE;
		if (exception instanceof OAuth2AuthenticationException oAuth2Ex) {
			String code = oAuth2Ex.getError().getErrorCode();
			if (code != null && !code.isBlank()) {
				errorCode = code;
			}
		}

		// 운영 디버깅용: 어떤 코드/메시지로 실패했는지 추적
		log.warn("OAuth2 로그인 실패: errorCode={}, message={}", errorCode, exception.getMessage());

		// URL 안전성을 위해 인코딩 (에러코드는 ASCII이지만 방어적 인코딩)
		String encodedErrorCode = URLEncoder.encode(errorCode, StandardCharsets.UTF_8);
		String redirectUrl = frontendBaseUrl + "/login?oauthError=" + encodedErrorCode;
		response.sendRedirect(redirectUrl);
	}

}
