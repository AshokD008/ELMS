package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.entity.Session;

import java.util.List;

public interface SessionService {

    Session createSession(
            Long userId,
            String accessToken,
            String ipAddress,
            String userAgent
    );

    Session getSessionById(Long sessionId);

    Session getSessionByToken(String accessToken);

    List<Session> getUserSessions(Long userId);

    PageResponse<Session> getAllSessions(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    Session updateSessionActivity(
            String accessToken
    );

    void logoutSession(String accessToken, String refreshToken);

    void logoutAllUserSessions(Long userId);

    void expireInactiveSessions();

    Boolean isSessionActive(String accessToken);

    void revokeSession(Long sessionId);

    void revokeAllSessionsExceptCurrent(
            Long userId,
            String currentToken
    );

    Long getActiveSessionCount(Long userId);
}
