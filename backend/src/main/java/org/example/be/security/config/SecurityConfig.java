package org.example.be.security.config;

import java.util.Arrays;
import java.util.List;

import org.example.be.jwt.util.JsonUt;
import org.example.be.oauth.handler.CustomSuccessHandler;
import org.example.be.oauth.service.CustomOAuth2UserService;
import org.example.be.response.CommonResponse;
import org.example.be.security.filter.CustomAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomSuccessHandler customSuccessHandler;
	private final CustomAuthenticationFilter customAuthenticationFilter;

	// @Bean
	// public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
	//
	// 	AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(
	// 		AuthenticationManagerBuilder.class);
	// 	authenticationManagerBuilder.authenticationProvider(restAuthenticationProvider);
	//
	// 	return authenticationManagerBuilder.build();
	// }

	// 비동기 방식 인증을 진행하기 위한 시큐리티 필터 체인
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/general-user/signup").permitAll()
				.requestMatchers(HttpMethod.POST, "/members").permitAll()
				.requestMatchers(HttpMethod.POST, "/members/login").permitAll()
				.requestMatchers("/oauth2/**").permitAll()
				.requestMatchers("/favicon.ico").permitAll()
				.requestMatchers("/.well-known/**").permitAll()
				.requestMatchers("/mail/**").permitAll()
				.requestMatchers("/board/**").permitAll()
				.requestMatchers("/comment/**").permitAll()
				.requestMatchers("/tourism/**").permitAll()
				.requestMatchers("/places/**").permitAll()
				.requestMatchers("/ws/**").permitAll()
				.requestMatchers("/error").permitAll()
				.requestMatchers(HttpMethod.GET, "/schedule/get/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/schedule/getList").authenticated()
				.anyRequest().authenticated()
			)
			// csrf 기능 끄기
			.csrf(AbstractHttpConfigurer::disable)
			// form login 끄기
			.formLogin(AbstractHttpConfigurer::disable)
			// httpBasic 끄기
			.httpBasic(AbstractHttpConfigurer::disable)
			// cors 설정
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			// 세션 사용 x stateless 상태 서버
			.sessionManagement(AbstractHttpConfigurer::disable)

			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

			.addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			// oauth2 설정
			.oauth2Login((oauth2) -> oauth2
				.userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
					.userService(customOAuth2UserService))
				.successHandler(customSuccessHandler))

			.exceptionHandling(
				exceptionHandling -> exceptionHandling
					.authenticationEntryPoint(
						(request, response, authException) -> {
							response.setContentType("application/json;charset=UTF-8");
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							CommonResponse<Void> errorResponse = CommonResponse.error(401, "로그인 후 이용해주세요.");
							response.getWriter().write(JsonUt.toString(errorResponse));
							response.getWriter().flush();
						}
					)
					.accessDeniedHandler(
						(request, response, accessDeniedException) -> {
							response.setContentType("application/json;charset=UTF-8");
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							CommonResponse<Void> errorResponse = CommonResponse.error(403, "권한이 없습니다.");
							response.getWriter().write(JsonUt.toString(errorResponse));
							response.getWriter().flush();
						}
					)
			);
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(
			List.of("https://localhost:3000", "https://localhost:5500", "https://toleave.cloud")); // 허용할 Origin
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Refresh-Token", "Content-Type"));
		configuration.setAllowCredentials(true); // 쿠키 허용
		configuration.setExposedHeaders(Arrays.asList("Authorization", "Refresh-Token", "Content-Type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}