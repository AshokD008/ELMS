package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.entity.UserRole;
import com.lms.usermanagementservice.exception.InvalidTokenException;
import com.lms.usermanagementservice.repository.SessionRepository;
import com.lms.usermanagementservice.security.jwt.JwtTokenProvider;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.JwtService;
import com.lms.usermanagementservice.util.SecurityUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private static final String BLACKLIST_PREFIX = "JWT_BLACKLIST:";
    private static final String USER_TOKEN_PREFIX = "USER_TOKEN:";

    private final JwtTokenProvider jwtTokenProvider;

    private final RedisTemplate<String, Object> redisTemplate;

    private final SessionRepository sessionRepository;

    private final AuditLogService auditLogService;

    private final SecurityUtil securityUtil;

    @Override
    public String generateAccessToken(User user) {

        Map<String, Object> claims = buildClaims(user);

        String token = jwtTokenProvider.generateToken(
                claims,
                user.getEmail()
        );

        cacheUserToken(user.getId(), token);

        log.info(LogConstants.JWT_ACCESS_TOKEN_GENERATED);

        return token;
    }

    @Override
    public String generateRefreshToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", user.getId());
        claims.put("type", "REFRESH");

        String token = jwtTokenProvider.generateRefreshToken(
                claims,
                user.getEmail()
        );

        cacheUserToken(user.getId(), token);

        log.info(LogConstants.JWT_REFRESH_TOKEN_GENERATED);

        return token;
    }

    @Override
    public String generateToken(UserDetails userDetails) {

        return jwtTokenProvider.generateToken(
                new HashMap<>(),
                userDetails.getUsername()
        );
    }

    @Override
    public String generateToken(
            UserDetails userDetails,
            Map<String, Object> claims
    ) {

        return jwtTokenProvider.generateToken(
                claims,
                userDetails.getUsername()
        );
    }

    @Override
    public Boolean validateToken(
            String token,
            UserDetails userDetails
    ) {

        try {

            String username = extractUsername(token);

            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token)
                    && !isTokenBlacklisted(token);

        } catch (ExpiredJwtException ex) {

            log.error("JWT token expired");

            throw new InvalidTokenException(
                    MessageConstants.JWT_TOKEN_EXPIRED
            );

        } catch (MalformedJwtException ex) {

            log.error("Malformed JWT token");

            throw new InvalidTokenException(
                    MessageConstants.INVALID_JWT_TOKEN
            );

        } catch (SignatureException ex) {

            log.error("Invalid JWT signature");

            throw new InvalidTokenException(
                    MessageConstants.INVALID_JWT_SIGNATURE
            );

        } catch (JwtException ex) {

            log.error("JWT validation failed");

            throw new InvalidTokenException(
                    MessageConstants.INVALID_JWT_TOKEN
            );
        }
    }

    @Override
    public Boolean isTokenExpired(String token) {

        return jwtTokenProvider
                .extractExpiration(token)
                .before(new java.util.Date());
    }

    @Override
    public String extractUsername(String token) {

        validateRawToken(token);

        return jwtTokenProvider.extractUsername(token);
    }

    @Override
    public Long extractUserId(String token) {

        Claims claims = extractClaims(token);

        Object userId = claims.get("userId");

        if (userId == null) {
            throw new InvalidTokenException(
                    MessageConstants.INVALID_JWT_TOKEN
            );
        }

        return Long.parseLong(userId.toString());
    }

    @Override
    public String extractRole(String token) {

        Claims claims = extractClaims(token);

        Object role = claims.get("role");

        return role != null
                ? role.toString()
                : null;
    }

    @Override
    public String extractTokenType(String token) {

        Claims claims = extractClaims(token);

        Object type = claims.get("type");

        return type != null
                ? type.toString()
                : "ACCESS";
    }

    @Override
    public String extractJti(String token) {

        Claims claims = extractClaims(token);

        return claims.getId();
    }

    @Override
    @Transactional
    public void blacklistToken(String token) {

        validateRawToken(token);

        Long userId = extractUserId(token);

        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "BLACKLISTED",
                24,
                TimeUnit.HOURS
        );

        sessionRepository.updateSessionStatusByAccessToken(
                token,
                "LOGGED_OUT"
        );

        auditLogService.createAuditLog(
                userId,
                "JWT_BLACKLISTED",
                "JWT token blacklisted"
        );

        log.info(LogConstants.JWT_TOKEN_BLACKLISTED);
    }

    @Override
    public Boolean isTokenBlacklisted(String token) {

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(
                        BLACKLIST_PREFIX + token
                )
        );
    }

    @Override
    @Transactional
    public void invalidateUserTokens(Long userId) {

        User currentUser = securityUtil.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            throw new InvalidTokenException(
                    MessageConstants.ACCESS_DENIED
            );
        }

        List<Object> tokens =
                redisTemplate.opsForList().range(
                        USER_TOKEN_PREFIX + userId,
                        0,
                        -1
                );

        if (!CollectionUtils.isEmpty(tokens)) {

            tokens.forEach(token -> {

                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + token,
                        "BLACKLISTED",
                        24,
                        TimeUnit.HOURS
                );
            });
        }

        redisTemplate.delete(USER_TOKEN_PREFIX + userId);

        sessionRepository.logoutAllUserSessions(
                userId
        );

        auditLogService.createAuditLog(
                userId,
                "ALL_TOKENS_INVALIDATED",
                "All JWT tokens invalidated"
        );

        log.info(LogConstants.JWT_ALL_TOKENS_INVALIDATED);
    }

    private Claims extractClaims(String token) {

        validateRawToken(token);

        try {

            return jwtTokenProvider.extractAllClaims(token);

        } catch (Exception ex) {

            log.error("Failed to extract JWT claims");

            throw new InvalidTokenException(
                    MessageConstants.INVALID_JWT_TOKEN
            );
        }
    }

    private void validateRawToken(String token) {

        if (!StringUtils.hasText(token)) {
            throw new InvalidTokenException(
                    MessageConstants.INVALID_JWT_TOKEN
            );
        }

        if (isTokenBlacklisted(token)) {
            throw new InvalidTokenException(
                    MessageConstants.JWT_TOKEN_BLACKLISTED
            );
        }
    }

    private Map<String, Object> buildClaims(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("status", user.getStatus().name());
        claims.put("type", "ACCESS");

        if (!CollectionUtils.isEmpty(user.getUserRoles())) {

            UserRole role = user.getUserRoles()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (role != null && role.getRole() != null) {
                claims.put(
                        "role",
                        role.getRole().getName()
                );
            }
        }

        return claims;
    }

    private void cacheUserToken(
            Long userId,
            String token
    ) {

        redisTemplate.opsForList().rightPush(
                USER_TOKEN_PREFIX + userId,
                token
        );

        redisTemplate.expire(
                USER_TOKEN_PREFIX + userId,
                Duration.ofDays(30)
        );
    }
}