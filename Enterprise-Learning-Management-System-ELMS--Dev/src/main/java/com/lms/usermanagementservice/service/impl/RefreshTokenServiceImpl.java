package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.response.TokenResponse;
import com.lms.usermanagementservice.entity.RefreshToken;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.UserStatus;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.InvalidTokenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.repository.RefreshTokenRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.RefreshTokenService;
import com.lms.usermanagementservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Integer REFRESH_TOKEN_EXPIRY_DAYS = 30;
    private static final String REFRESH_TOKEN_CACHE_PREFIX = "REFRESH_TOKEN:";

    private final RefreshTokenRepository refreshTokenRepository;

    private final AuditLogService auditLogService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {

        log.info(LogConstants.REFRESH_TOKEN_CREATED);

        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS)
        );

        RefreshToken savedToken =
                refreshTokenRepository.save(refreshToken);

        cacheRefreshToken(savedToken);

        auditLogService.createAuditLog(
                user.getId(),
                "REFRESH_TOKEN_CREATED",
                "Refresh token created successfully"
        );

        return savedToken;
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {

        if (!StringUtils.hasText(token)) {
            throw new InvalidTokenException(
                    MessageConstants.INVALID_REFRESH_TOKEN
            );
        }

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        MessageConstants.INVALID_REFRESH_TOKEN
                                ));

        if (refreshToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            refreshTokenRepository.delete(refreshToken);

            clearRefreshTokenCache(token);

            throw new InvalidTokenException(
                    MessageConstants.REFRESH_TOKEN_EXPIRED
            );
        }

        User user = refreshToken.getUser();

        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new ForbiddenException(
                    MessageConstants.ACCOUNT_BLOCKED
            );
        }

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ForbiddenException(
                    MessageConstants.ACCOUNT_DELETED
            );
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public TokenResponse revokeRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.REFRESH_TOKEN_NOT_FOUND
                                ));

        User currentUser = securityUtil.getCurrentUser();

        if (!refreshToken.getUser().getId()
                .equals(currentUser.getId())) {

            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }

        refreshTokenRepository.delete(refreshToken);

        clearRefreshTokenCache(token);

        auditLogService.createAuditLog(
                currentUser.getId(),
                "REFRESH_TOKEN_REVOKED",
                "Refresh token revoked successfully"
        );

        return TokenResponse.builder()
                .success(true)
                .message(MessageConstants.REFRESH_TOKEN_REVOKED)
                .build();
    }

    @Override
    @Transactional
    public TokenResponse revokeAllUserRefreshTokens(Long userId) {

        User currentUser = securityUtil.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }

        List<RefreshToken> tokens =
                refreshTokenRepository.findAllByUserId(userId);

        if (!tokens.isEmpty()) {

            refreshTokenRepository.deleteAll(tokens);

            tokens.forEach(token ->
                    clearRefreshTokenCache(token.getToken())
            );
        }

        auditLogService.createAuditLog(
                userId,
                "ALL_REFRESH_TOKENS_REVOKED",
                "All refresh tokens revoked"
        );

        return TokenResponse.builder()
                .success(true)
                .message(MessageConstants.ALL_REFRESH_TOKENS_REVOKED)
                .build();
    }

    @Override
    public Boolean validateRefreshToken(String token) {

        try {

            RefreshToken refreshToken =
                    verifyRefreshToken(token);

            return refreshToken != null;

        } catch (Exception ex) {

            log.error("Refresh token validation failed");

            return false;
        }
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken) {

        RefreshToken existingToken =
                verifyRefreshToken(oldToken);

        User user = existingToken.getUser();

        refreshTokenRepository.delete(existingToken);

        clearRefreshTokenCache(oldToken);

        RefreshToken newToken =
                createRefreshToken(user);

        auditLogService.createAuditLog(
                user.getId(),
                "REFRESH_TOKEN_ROTATED",
                "Refresh token rotated successfully"
        );

        return newToken;
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {

        List<RefreshToken> expiredTokens =
                refreshTokenRepository
                        .findAllByExpiryDateBefore(
                                LocalDateTime.now()
                        );

        if (expiredTokens.isEmpty()) {
            return;
        }

        expiredTokens.forEach(token ->
                clearRefreshTokenCache(token.getToken())
        );

        refreshTokenRepository.deleteAll(expiredTokens);

        log.info("Expired refresh tokens deleted successfully");
    }

    @Override
    public String extractTokenFromHeader(String bearerToken) {

        if (!StringUtils.hasText(bearerToken)) {
            throw new InvalidTokenException(
                    MessageConstants.INVALID_TOKEN
            );
        }

        if (!bearerToken.startsWith("Bearer ")) {
            throw new InvalidTokenException(
                    MessageConstants.INVALID_TOKEN_FORMAT
            );
        }

        return bearerToken.substring(7);
    }

    private void cacheRefreshToken(RefreshToken refreshToken) {

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_CACHE_PREFIX
                        + refreshToken.getToken(),
                refreshToken.getUser().getId(),
                REFRESH_TOKEN_EXPIRY_DAYS,
                TimeUnit.DAYS
        );
    }

    private void clearRefreshTokenCache(String token) {

        redisTemplate.delete(
                REFRESH_TOKEN_CACHE_PREFIX + token
        );
    }
}
