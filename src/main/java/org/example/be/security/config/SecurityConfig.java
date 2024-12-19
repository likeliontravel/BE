package org.example.be.security.config;

import lombok.RequiredArgsConstructor;
import org.example.be.security.filter.RestAuthenticationFilter;
import org.example.be.security.handler.RestAuthenticationFailureHandler;
import org.example.be.security.handler.RestAuthenticationSuccessHandler;
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

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/SignUp").permitAll()
                        .anyRequest().authenticated()
                )

                // 필터 추가하기 UsernamePasswordAuthenticationFilter 이전 위치에 restAuthenticationFilter 위치 하도록 함
                .addFilterBefore(restAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
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

        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // 허용할 Origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true); // 쿠키 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}