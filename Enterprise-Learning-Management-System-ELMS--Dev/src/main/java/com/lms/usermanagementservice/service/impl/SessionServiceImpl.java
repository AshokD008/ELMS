package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.entity.Session;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.SessionStatus;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.repository.SessionRepository;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.repository.RefreshTokenRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.JwtService;
import com.lms.usermanagementservice.service.SessionService;
import com.lms.usermanagementservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private static final String SESSION_CACHE_PREFIX =
            "SESSION:";
    private static final Integer SESSION_TIMEOUT_MINUTES =
            30;

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtService jwtService;

    private final SecurityUtil securityUtil;

    private final AuditLogService auditLogService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public Session createSession(
            Long userId,
            String accessToken,
            String ipAddress,
            String userAgent
    ) {

        log.info(LogConstants.SESSION_CREATE_INITIATED);

        User currentUser = securityUtil.getCurrentUser();
        if (currentUser == null || !currentUser.getId().equals(userId)
                || !userId.equals(jwtService.extractUserId(accessToken))
                || !"ACCESS".equals(jwtService.extractTokenType(accessToken))
                || jwtService.isTokenExpired(accessToken)) {
            throw new ForbiddenException(MessageConstants.ACCESS_DENIED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND
                        ));

        Session session = new Session();

        session.setUser(user);
        session.setSessionToken(accessToken);
        session.setSessionId(
                UUID.randomUUID().toString()
        );
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setStatus(SessionStatus.ACTIVE);
        session.setLoginAt(LocalDateTime.now());
        session.setLastAccessTime(LocalDateTime.now());

        Session savedSession =
                sessionRepository.save(session);

        cacheSession(savedSession);

        auditLogService.createAuditLog(
                userId,
                "SESSION_CREATED",
                "User session created"
        );

        log.info(LogConstants.SESSION_CREATE_SUCCESS);

        return savedSession;
    }

    @Override
    public Session getSessionById(
            Long sessionId
    ) {

        return sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.SESSION_NOT_FOUND
                        ));
    }

    @Override
    public Session getSessionByToken(
            String accessToken
    ) {

        Session session = sessionRepository
                .findBySessionToken(accessToken)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.SESSION_NOT_FOUND
                        ));
        validateSessionAccess(securityUtil.getCurrentUser(), session.getUser().getId());
        return session;
    }

    @Override
    public List<Session> getUserSessions(
            Long userId
    ) {

        User currentUser =
                securityUtil.getCurrentUser();

        validateSessionAccess(
                currentUser,
                userId
        );

        return sessionRepository
                .findAllByUserId(userId);
    }

    @Override
    public PageResponse<Session> getAllSessions(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {

        validateAdminAccess();

        Sort sort =
                sortDirection.equalsIgnoreCase("DESC")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Page<Session> sessionPage =
                sessionRepository.findAll(pageable);

        return PageResponse.<Session>builder()
                .content(sessionPage.getContent())
                .page(page)
                .size(size)
                .totalPages(sessionPage.getTotalPages())
                .totalElements(sessionPage.getTotalElements())
                .last(sessionPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public Session updateSessionActivity(
            String accessToken
    ) {

        Session session =
                sessionRepository
                        .findBySessionToken(accessToken)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.SESSION_NOT_FOUND
                                ));

        validateSessionAccess(securityUtil.getCurrentUser(), session.getUser().getId());

        if (session.getStatus()
                != SessionStatus.ACTIVE) {

            throw new ForbiddenException(
                    MessageConstants.SESSION_EXPIRED
            );
        }

        session.setLastAccessTime(
                LocalDateTime.now()
        );

        Session updatedSession =
                sessionRepository.save(session);

        cacheSession(updatedSession);

        return updatedSession;
    }

    @Override
    @Transactional
    public void logoutSession(
            String accessToken,
            String refreshToken
    ) {

        Session session =
                sessionRepository
                        .findBySessionToken(accessToken)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.SESSION_NOT_FOUND
                                ));

        User currentUser =
                securityUtil.getCurrentUser();

        if (!session.getUser().getId()
                .equals(currentUser.getId())) {

            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }

        session.setStatus(
                SessionStatus.LOGGED_OUT
        );

        session.setLogoutAt(
                LocalDateTime.now()
        );

        sessionRepository.save(session);

        var storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new com.lms.usermanagementservice.exception.InvalidTokenException(
                        MessageConstants.INVALID_REFRESH_TOKEN));
        if (!storedRefreshToken.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(MessageConstants.ACCESS_DENIED);
        }
        refreshTokenRepository.delete(storedRefreshToken);

        jwtService.blacklistToken(accessToken);

        redisTemplate.delete(
                SESSION_CACHE_PREFIX
                        + session.getSessionId()
        );

        auditLogService.createAuditLog(
                currentUser.getId(),
                "SESSION_LOGOUT",
                "Session logged out"
        );
    }

    @Override
    @Transactional
    public void logoutAllUserSessions(
            Long userId
    ) {

        User currentUser =
                securityUtil.getCurrentUser();

        validateSessionAccess(
                currentUser,
                userId
        );

        List<Session> sessions =
                sessionRepository
                        .findAllByUserId(userId);

        sessions.forEach(session -> {

            session.setStatus(
                    SessionStatus.LOGGED_OUT
            );

            session.setLogoutAt(
                    LocalDateTime.now()
            );

            jwtService.blacklistToken(
                    session.getAccessToken()
            );

            redisTemplate.delete(
                    SESSION_CACHE_PREFIX
                            + session.getSessionId()
            );
        });

        sessionRepository.saveAll(sessions);

        auditLogService.createAuditLog(
                userId,
                "ALL_SESSIONS_LOGOUT",
                "All user sessions logged out"
        );
    }

    @Override
    @Transactional
    public void expireInactiveSessions() {

        LocalDateTime expiryTime =
                LocalDateTime.now()
                        .minusMinutes(
                                SESSION_TIMEOUT_MINUTES
                        );

        List<Session> inactiveSessions =
                sessionRepository
                        .findAllByLastAccessTimeBeforeAndStatus(
                                expiryTime,
                                SessionStatus.ACTIVE
                        );

        inactiveSessions.forEach(session -> {

            session.setStatus(
                    SessionStatus.EXPIRED
            );

            session.setLogoutAt(
                    LocalDateTime.now()
            );

            redisTemplate.delete(
                    SESSION_CACHE_PREFIX
                            + session.getSessionId()
            );
        });

        sessionRepository.saveAll(inactiveSessions);

        log.info("Inactive sessions expired successfully");
    }

    @Override
    public Boolean isSessionActive(
            String accessToken
    ) {

        return sessionRepository
                .findBySessionToken(accessToken)
                .map(session ->
                        session.getStatus()
                                == SessionStatus.ACTIVE
                )
                .orElse(false);
    }

    @Override
    @Transactional
    public void revokeSession(
            Long sessionId
    ) {

        validateAdminAccess();

        Session session =
                sessionRepository.findById(sessionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.SESSION_NOT_FOUND
                                ));

        session.setStatus(
                SessionStatus.REVOKED
        );

        session.setLogoutAt(
                LocalDateTime.now()
        );

        sessionRepository.save(session);

        jwtService.blacklistToken(
                session.getAccessToken()
        );

        redisTemplate.delete(
                SESSION_CACHE_PREFIX
                        + session.getSessionId()
        );

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "SESSION_REVOKED",
                "Session revoked by admin"
        );
    }

    @Override
    @Transactional
    public void revokeAllSessionsExceptCurrent(
            Long userId,
            String currentToken
    ) {

        User currentUser =
                securityUtil.getCurrentUser();

        validateSessionAccess(
                currentUser,
                userId
        );

        List<Session> sessions =
                sessionRepository
                        .findAllByUserId(userId);

        sessions.stream()
                .filter(session ->
                        !session.getAccessToken()
                                .equals(currentToken)
                )
                .forEach(session -> {

                    session.setStatus(
                            SessionStatus.REVOKED
                    );

                    session.setLogoutAt(
                            LocalDateTime.now()
                    );

                    jwtService.blacklistToken(
                            session.getAccessToken()
                    );

                    redisTemplate.delete(
                            SESSION_CACHE_PREFIX
                                    + session.getSessionId()
                    );
                });

        sessionRepository.saveAll(sessions);

        auditLogService.createAuditLog(
                userId,
                "OTHER_SESSIONS_REVOKED",
                "All sessions except current revoked"
        );
    }

    @Override
    public Long getActiveSessionCount(
            Long userId
    ) {

        validateSessionAccess(securityUtil.getCurrentUser(), userId);

        return sessionRepository
                .countByUserIdAndStatus(
                        userId,
                        SessionStatus.ACTIVE
                );
    }

    private void validateSessionAccess(
            User currentUser,
            Long userId
    ) {

        boolean isAdmin =
                currentUser.getUserRoles()
                        .stream()
                        .anyMatch(role ->
                                role.getRole()
                                        .getName()
                                        .equals(
                                                UserRoleType.SUPER_ADMIN.name()
                                        )
                        );

        if (!isAdmin
                && !currentUser.getId().equals(userId)) {

            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }
    }

    private void validateAdminAccess() {

        User currentUser =
                securityUtil.getCurrentUser();

        boolean isAdmin =
                currentUser.getUserRoles()
                        .stream()
                        .anyMatch(role ->
                                role.getRole()
                                        .getName()
                                        .equals(
                                                UserRoleType.SUPER_ADMIN.name()
                                        )
                        );

        if (!isAdmin) {

            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }
    }

    private void cacheSession(
            Session session
    ) {

        HashMap<String, Object> cacheValue = new HashMap<>();
        cacheValue.put("id", session.getId());
        cacheValue.put("sessionId", session.getSessionId());
        cacheValue.put("userId", session.getUser() == null ? null : session.getUser().getId());
        cacheValue.put("status", session.getStatus());
        cacheValue.put("loginAt", session.getLoginAt());
        cacheValue.put("logoutAt", session.getLogoutAt());

        redisTemplate.opsForValue().set(
                SESSION_CACHE_PREFIX
                        + session.getSessionId(),
                cacheValue,
                Duration.ofMinutes(
                        SESSION_TIMEOUT_MINUTES
                )
        );
    }
}
