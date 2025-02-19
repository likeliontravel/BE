package org.example.be.jwt.util;

import io.jsonwebtoken.*;
import org.example.be.oauth.dto.CustomOAuth2User;
import org.example.be.oauth.dto.SocialUserDTO;
import org.example.be.oauth.entity.SocialUser;
import org.example.be.oauth.repository.SocialUserRepository;
import org.example.be.oauth.service.CustomOAuth2UserService;
import org.example.be.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JWTUtil {

    private final SecretKey secretKey;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final SocialUserRepository socialUserRepository;


    public JWTUtil(@Value("${spring.jwt.secret}") String secret, CustomUserDetailsService customUserDetailsService,
                   CustomOAuth2UserService customOAuth2UserService, SocialUserRepository socialUserRepository) {

        if (secret.length() < 32) {

            ///  비밀 키가 32 자리가 아니면 예외 발생
           throw new IllegalArgumentException("JWT secret key must be at least 32 characters long");
        }

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.customUserDetailsService = customUserDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.socialUserRepository = socialUserRepository;
    }
    /*
     * JWT 토큰에서 만료 시간(Expiration) 추출 */
    public Date getExpiration(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("토큰 만료됨: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.out.println("서명이 올바르지 않은 토큰: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.out.println("손상된 토큰: " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            System.out.println("지원되지 않는 토큰 형식: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("유효하지 않은 토큰: " + e.getMessage());
            return false;
        }
    }

    // 토큰을 이용해 인증객체 가져오는 메서드
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userIdentifier = claims.get("userIdentifier", String.class);
        String role = claims.get("role", String.class);

        // 일반 로그인 유저 조회
        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userIdentifier);
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } catch (UsernameNotFoundException e) {
            // 일반 유저가 아니라면 조회되지 않으므로 소셜 로그인 유저 조회
            SocialUser socialUser = socialUserRepository.findByUserIdentifier(userIdentifier)
                    .orElseThrow(() -> new UsernameNotFoundException("소셜 로그인 사용자도 조회할 수 없음: " + userIdentifier));

            SocialUserDTO socialUserDTO = new SocialUserDTO();
            socialUserDTO.setId(socialUser.getId());
            socialUserDTO.setEmail(socialUser.getEmail());
            socialUserDTO.setName(socialUser.getName());
            socialUserDTO.setProvider(socialUser.getProvider());
            socialUserDTO.setRole(socialUser.getRole());
            socialUserDTO.setUserIdentifier(socialUser.getUserIdentifier());

            return new UsernamePasswordAuthenticationToken(new CustomOAuth2User(socialUserDTO), null, List.of(new SimpleGrantedAuthority(role)));
        }
    }

    // 토큰에서 userIdentifier 가져오는 메서드
    public String getUserIdentifier(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userIdentifier", String.class);
    }

    // 토큰에서 role 가져오는 메서드
    public String getRole(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
    }
}
