package com.example.secure_file_sharing.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${app.jwt.secret}")
	private String secret;

	@Value("${app.jwt.expiration-ms}")
	private long expiration;

	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String email, String role) {
		Date now = new Date();

		return Jwts.builder().subject(email).claim("role", role).issuedAt(now)
				.expiration(new Date(now.getTime() + expiration)).signWith(getKey()).compact();
	}

	public String extractEmail(String token) {
		return extractClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, String email) {
		Claims claims = extractClaims(token);

		return claims.getSubject().equals(email) && claims.getExpiration().after(new Date());
	}

	private Claims extractClaims(String token) {
		return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
	}
}
