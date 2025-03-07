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

// jwt 생성 및 제공을 맡을 클래스 JWTProvider
@Component
public class JWTProvider {

    private final SecretKey secretKey;

    public JWTProvider(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성 ( 2분 )
    public String generateAccessToken(String userIdentifier, String role) {
        return Jwts.builder()
                .setSubject("accessToken")
                .claim("userIdentifier", userIdentifier)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 2))    // 2분
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성 ( 7분 )
    public String generateRefreshToken(String userIdentifier, String role) {
        return Jwts.builder()
                .setSubject("refreshToken")
                .claim("userIdentifier", userIdentifier)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 7))    // 7분
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
