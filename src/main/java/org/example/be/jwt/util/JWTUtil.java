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

/*
    ## JWT 검증 시 필요한 메서드를 정의해둘 클래스 JWTUtil ##
*/
@Component
public class JWTUtil {

    private final SecretKey secretKey;
    private final CustomUserDetailsService customUserDetailsService;
    private final SocialUserRepository socialUserRepository;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret,
                   CustomUserDetailsService customUserDetailsService,
                   SocialUserRepository socialUserRepository) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.customUserDetailsService = customUserDetailsService;
        this.socialUserRepository = socialUserRepository;
    }

    // JWT 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException | UnsupportedJwtException e) {
            System.out.println("JWT 검증 중 다음 예외 발생: " + e.getMessage());
            return false;
        }
    }

    // 토큰에서 userIdentifie만 추출
    public String getUserIdentifier(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
                .getPayload().get("userIdentifier", String.class);
    }

    // 토큰에서 role만 추출
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
                .getPayload().get("role", String.class);
    }


    /* JWT 토큰에서 만료 시간(Expiration) 추출 */
    public Date getExpiration(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration();
    }


    // 토큰을 이용해 인증객체 가져오는 메서드
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload();

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

}
