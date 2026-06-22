package com.lms.usermanagementservice.util;

import com.lms.usermanagementservice.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;

    public String generateAccessToken(String username) {

        Date now = new Date();

        Date expiryDate = new Date(
                now.getTime() + jwtConfig.getAccessTokenExpiration()
        );

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {

        Date now = new Date();

        Date expiryDate = new Date(
                now.getTime() + jwtConfig.getRefreshTokenExpiration()
        );

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {

        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {

        try {

            Claims claims = getClaims(token);

            return claims.getExpiration().after(new Date());

        } catch (Exception ex) {

            return false;
        }
    }

    private Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {

        byte[] keyBytes = jwtConfig.getSecret()
                .getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}