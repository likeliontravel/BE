package org.example.be.jwt.provider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.example.be.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTProvider {

    private final CustomUserDetailsService customUserDetailsService;
    private final SecretKey secretKey;

    public JWTProvider(@Value("${spring.jwt.secret}") String secret,
                       CustomUserDetailsService customUserDetailsService) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.customUserDetailsService = customUserDetailsService;
    }

    //유저 인증객체 생성
    public Authentication getUserDetails(String username) {

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    // access토큰 생성 메서드
    public String generateAccessToken(String userIdentifier, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + 2 * 60 * 1000); // 2분

        System.out.println("JWTProvider.generateAccessToken()에 들어온 userIdentifier : " + userIdentifier + ", role : " + role);
        return Jwts.builder()
                .setSubject("accessToken")
                .claim("userIdentifier", userIdentifier)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // refresh token 생성 메서드
    public String generateRefreshToken(String userIdentifier, String role) {
        Date now = new Date();
//        Date validity = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);  // 7일
        Date validity = new Date(now.getTime() + 7 * 60 * 1000);  // 7분

        System.out.println("JWTProvider.generateRefreshToken()에 들어온 userIdentifier : " + userIdentifier + ", role : " + role);
        return Jwts.builder()
                .setSubject("refreshToken")
                .claim("userIdentifier", userIdentifier)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
