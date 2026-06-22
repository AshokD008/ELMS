package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String generateToken(UserDetails userDetails);

    String generateToken(
            UserDetails userDetails,
            Map<String, Object> claims
    );

    Boolean validateToken(
            String token,
            UserDetails userDetails
    );

    Boolean isTokenExpired(String token);

    String extractUsername(String token);

    Long extractUserId(String token);

    String extractRole(String token);

    String extractTokenType(String token);

    String extractJti(String token);

    void blacklistToken(String token);

    Boolean isTokenBlacklisted(String token);

    void invalidateUserTokens(Long userId);
}