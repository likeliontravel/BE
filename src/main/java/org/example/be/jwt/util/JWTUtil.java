package org.example.be.jwt.util;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {

        if (secret.length() < 32) {

            ///  비밀 키가 32 자리가 아니면 예외 발생
           throw new IllegalArgumentException("JWT secret key must be at least 32 characters long");
        }

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /*
    * JWT 토큰에서 유저 이름 추출
    * email 이였음 */
    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    // JWT 토큰에서 역할 추출
    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    /*
    * JWT 토큰에서 만료 시간 추출
    * 만료 시간이 현재보다 이전이라면, 토큰은 만료된 것으로 간주 */
    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    /*
     * JWT 토큰에서 만료 시간(Expiration) 추출 */
    public Date getExpiration(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration();
    }

    /*
    * JWT 토큰 검증 로직 */
    public Boolean isValid(String token) {

        try {

            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // JWT 생성 하는 함수
    public String createJwt(String username, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }
}
