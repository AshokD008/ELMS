package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.response.TokenResponse;
import com.lms.usermanagementservice.entity.RefreshToken;
import com.lms.usermanagementservice.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    TokenResponse revokeRefreshToken(String token);

    TokenResponse revokeAllUserRefreshTokens(Long userId);

    Boolean validateRefreshToken(String token);

    RefreshToken rotateRefreshToken(String oldToken);

    void deleteExpiredTokens();

    String extractTokenFromHeader(String bearerToken);
}