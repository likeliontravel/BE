package org.example.be.security.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.be.jwt.service.JWTBlackListService;
import org.example.be.jwt.util.JWTUtil;
import org.example.be.jwt.filter.JWTFilter;
import org.example.be.jwt.provider.JWTProvider;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.example.be.oauth.handler.CustomSuccessHandler;
import org.example.be.oauth.service.CustomOAuth2UserService;
import org.example.be.security.filter.RestAuthenticationFilter;
import org.example.be.security.handler.*;
import org.example.be.security.provider.RestAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestAuthenticationProvider restAuthenticationProvider;
    private final RestAuthenticationFailureHandler restAuthenticationFailureHandler;
    private final RestAuthenticationSuccessHandler restAuthenticationSuccessHandler;
    private final RestLogoutHandler restLogoutHandler;
    private final RestLogoutSuccessHandler restLogoutSuccessHandler;
    private final JWTProvider jwtProvider;
    private final JWTUtil jwtUtil;
    private final JWTBlackListService jwtBlackListService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(restAuthenticationProvider);

        return authenticationManagerBuilder.build();
    }

    // 비동기 방식 인증을 진행하기 위한 시큐리티 필터 체인
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

        http
                // csrf 기능 끄기
                .csrf(AbstractHttpConfigurer::disable)
                // form login 끄기
                .formLogin(AbstractHttpConfigurer::disable)
                // httpBasic 끄기
                .httpBasic(AbstractHttpConfigurer::disable)
                // cors 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 세션 사용 x stateless 상태 서버
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // oauth2 설정
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                    System.out.println("🚨 OAuth2 로그인 실패: " + exception.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"OAuth2 로그인 실패: " + exception.getMessage() + "\"}");
                })
                )


                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/general-user/SignUp", "/general-user/login").permitAll()
                        .requestMatchers("/mail/**", "/user/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll() // OAuth2 로그인 경로 추가
                        .anyRequest().authenticated()
                )

                // 필터 추가하기 UsernamePasswordAuthenticationFilter 이전 위치에 restAuthenticationFilter 위치 하도록 함
                .addFilterBefore(restAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                // JWT 필터 추가 RestAuthenticationFilter 이전에 추가
                .addFilterBefore(new JWTFilter(jwtUtil, jwtProvider, jwtBlackListService), RestAuthenticationFilter.class)

                // 로그아웃 필터 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(restLogoutHandler)
                        .logoutSuccessHandler(restLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true))

                // 접근 금지 핸들러랑 권한 없는 엔트리 포인트 작성 및 사용 완료
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                        .accessDeniedHandler(new RestAccessDeniedHandler())
                )
        ;

        return http.build();
    }

    private RestAuthenticationFilter restAuthenticationFilter(AuthenticationManager authenticationManager) {

        RestAuthenticationFilter restAuthenticationFilter = new RestAuthenticationFilter();

        restAuthenticationFilter.setAuthenticationManager(authenticationManager);

        restAuthenticationFilter.setAuthenticationFailureHandler(restAuthenticationFailureHandler);
        restAuthenticationFilter.setAuthenticationSuccessHandler(restAuthenticationSuccessHandler);

        return restAuthenticationFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of("https://toleave.shop")); // CORS 허용 도메인
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Refresh-Token", "User-Identifier"));

        // ✅ 변경: exposeHeaders가 항상 올바르게 설정되도록 변경
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Refresh-Token", "User-Identifier"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
