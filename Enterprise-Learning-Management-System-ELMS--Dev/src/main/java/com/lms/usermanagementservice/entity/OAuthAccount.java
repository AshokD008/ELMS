package com.lms.usermanagementservice.entity;

import com.lms.usermanagementservice.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "oauth_accounts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
        },
        indexes = {
                @Index(name = "idx_oauth_user", columnList = "user_id"),
                @Index(name = "idx_oauth_provider", columnList = "provider"),
                @Index(name = "idx_oauth_provider_user", columnList = "provider_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "provider_email", length = 150)
    private String providerEmail;

    @Column(name = "provider_name", length = 150)
    private String providerName;

    @Column(name = "profile_url", length = 500)
    private String profileUrl;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "scope", length = 500)
    private String scope;

    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.connectedAt == null) {
            this.connectedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return providerEmail;
    }

    public void setEmail(String email) {
        this.providerEmail = email;
    }

    public LocalDateTime getLinkedAt() {
        return connectedAt;
    }

    public void setLinkedAt(LocalDateTime linkedAt) {
        this.connectedAt = linkedAt;
    }
}
