package com.lms.usermanagementservice.entity;

import com.lms.usermanagementservice.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sessions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "session_token")
        },
        indexes = {
                @Index(name = "idx_session_user", columnList = "user_id"),
                @Index(name = "idx_session_token", columnList = "session_token"),
                @Index(name = "idx_session_status", columnList = "status"),
                @Index(name = "idx_session_expiry", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_token", nullable = false, unique = true, length = 500)
    private String sessionToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SessionStatus status;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    // FIX: Changed from length=100 to TEXT definition to fully capture long browser User-Agent strings
    @Column(name = "browser", columnDefinition = "TEXT")
    private String browser;

    // FIX: Changed from length=150 to TEXT definition to safely capture complex mobile/desktop device details
    @Column(name = "device_name", columnDefinition = "TEXT")
    private String deviceName;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.loginAt == null) {
            this.loginAt = LocalDateTime.now();
        }

        if (this.expiresAt == null) {
            this.expiresAt = LocalDateTime.now().plusDays(30);
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getSessionId() {
        return sessionToken;
    }

    public void setSessionId(String sessionId) {
        this.sessionToken = sessionId;
    }

    public String getAccessToken() {
        return sessionToken;
    }

    public void setAccessToken(String accessToken) {
        this.sessionToken = accessToken;
    }

    public String getUserAgent() {
        return browser;
    }

    public void setUserAgent(String userAgent) {
        this.browser = userAgent;
    }

    public LocalDateTime getLastAccessTime() {
        return updatedAt != null ? updatedAt : loginAt;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.updatedAt = lastAccessTime;
    }
}