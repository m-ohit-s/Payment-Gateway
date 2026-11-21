package com.myspace.razorpayclone.merchant.security;

import com.myspace.razorpayclone.merchant.service.IAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String secretKey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(
            String email,
            UUID merchantId,
            String role,
            Long tokenVersion
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60 * 10)))
                .claim("merchantId", merchantId.toString())
                .claim("role", role)
                .claim("tokenVersion", tokenVersion.toString())
                .signWith(getSecretKey())
                .compact();
    }


    public Claims verifyToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public UUID extractMerchantId(Claims claims) {
        return UUID.fromString(claims.get("merchantId", String.class));
    }
}
