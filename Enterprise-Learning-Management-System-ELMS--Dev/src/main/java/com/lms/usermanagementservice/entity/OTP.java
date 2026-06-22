package com.lms.usermanagementservice.entity;

import com.lms.usermanagementservice.enums.OTPStatus;
import com.lms.usermanagementservice.enums.OTPType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "otps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "otp_code", nullable = false, length = 20)
    private String otpCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false, length = 50)
    private OTPType otpType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OTPStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();

        if (this.expiresAt == null) {
            this.expiresAt = LocalDateTime.now().plusMinutes(5);
        }
    }

    public LocalDateTime getExpiryTime() {
        return expiresAt;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiresAt = expiryTime;
    }
}
