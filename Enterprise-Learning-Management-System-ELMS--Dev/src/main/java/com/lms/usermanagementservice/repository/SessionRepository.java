package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.Session;
import com.lms.usermanagementservice.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findBySessionToken(String sessionToken);

    List<Session> findAllByUserId(Long userId);

    List<Session> findByStatus(SessionStatus status);

    List<Session> findAllByUpdatedAtBeforeAndStatus(java.time.LocalDateTime updatedAt, SessionStatus status);

    default List<Session> findAllByLastAccessTimeBeforeAndStatus(java.time.LocalDateTime lastAccessTime, SessionStatus status) {
        return findAllByUpdatedAtBeforeAndStatus(lastAccessTime, status);
    }

    long countByUserIdAndStatus(Long userId, SessionStatus status);

    default void updateSessionStatusByAccessToken(String accessToken, String status) {
        findBySessionToken(accessToken).ifPresent(session -> session.setStatus(SessionStatus.valueOf(status)));
    }

    default void logoutAllUserSessions(Long userId) {
        findAllByUserId(userId).forEach(session -> session.setStatus(SessionStatus.EXPIRED));
    }
}
