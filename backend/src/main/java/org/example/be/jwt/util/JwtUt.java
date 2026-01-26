package org.example.be.jwt.util;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUt {
	public static String toString(String secret, int expireSeconds, Map<String, Object> body) {
		ClaimsBuilder claimsBuilder = Jwts.claims();

		for (Map.Entry<String, Object> entry : body.entrySet()) {
			claimsBuilder.add(entry.getKey(), entry.getValue());
		}

		Claims claims = claimsBuilder.build();

		Date issuedAt = new Date();
		Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);

		Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());

		String jwt = Jwts.builder()
			.claims(claims)
			.issuedAt(issuedAt)
			.expiration(expiration)
			.signWith(secretKey)
			.compact();

		return jwt;
	}

	public static boolean isValid(String secret, String jwtStr) {
		SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

		try {
			Jwts
				.parser()
				.verifyWith(secretKey)
				.build()
				.parse(jwtStr);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static Map<String, Object> payload(String secret, String jwtStr) {
		SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

		try {
			return (Map<String, Object>)Jwts
				.parser()
				.verifyWith(secretKey)
				.build()
				.parse(jwtStr)
				.getPayload();
		} catch (Exception e) {
			return null;
		}
	}

}
