package org.example.be.jwt.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
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
            throw new RuntimeException("JWT secret key must be at least 32 characters long");
        }

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /*
     * JWT 토큰에서 유저 이름 추출
     * email 이였음 */
    public String getUserIdentifier(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userIdentifier", String.class);
    }

    // JWT 토큰에서 역할 추출
    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    /*
     * JWT 토큰에서 만료 시간 추출
     * 만료 시간이 현재보다 이전이라면, 토큰은 만료된 것으로 간주 */
    public Boolean isExpired(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch(ExpiredJwtException e) {
            return true;
        }

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

            /*
                JJWT에서 내부 동작 흐름
                1. 토큰구조 / 서명형식 체크. 이상이 있을 시 MalformedJwtException 발생
                2. 서명 검증. 이상이 있을 시 SignatureException 발생
                3. 만료 시간 체크 (만료되면 ExpiredJwtException)

                즉, 만료시간을 가장 마지막에 체크하기 때문에 만료되어 발생한 예외처리를 true값으로 넘겨도 다른 예외의 가능성이 없다.
             */
        } catch (ExpiredJwtException e) {
            System.out.println("토큰이 만료됨");
            return true;
        } catch (MalformedJwtException e) {
            System.out.println("토큰 형식이 잘못됨");
            return false;
        } catch (Exception e) {
            System.out.println("jwtUtil.isValid() -> 기타 예외 : " + e.getClass().getSimpleName());
            return false;
        }
    }

    // JWT 생성 하는 함수
    public String createJwt(String userIdentifier, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("userIdentifier", userIdentifier)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }
}
