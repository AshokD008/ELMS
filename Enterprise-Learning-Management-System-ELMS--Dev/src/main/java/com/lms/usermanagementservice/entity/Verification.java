package com.lms.usermanagementservice.entity;

import com.lms.usermanagementservice.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "verifications",
        indexes = {
                @Index(name = "idx_verification_user", columnList = "user_id"),
                @Index(name = "idx_verification_token", columnList = "verification_token"),
                @Index(name = "idx_verification_status", columnList = "status"),
                @Index(name = "idx_verification_expiry", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_token", nullable = false, length = 500)
    private String verificationToken;

    @Column(name = "verification_type", nullable = false, length = 100)
    private String verificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private VerificationStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "request_ip_address", length = 100)
    private String requestIpAddress;

    @Column(name = "verified_ip_address", length = 100)
    private String verifiedIpAddress;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getDocumentType() {
        return verificationType;
    }

    public void setDocumentType(String documentType) {
        this.verificationType = documentType;
    }

    public String getDocumentNumber() {
        return verificationToken;
    }

    public void setDocumentNumber(String documentNumber) {
        this.verificationToken = documentNumber;
    }

    public String getDocumentUrl() {
        return remarks;
    }

    public void setDocumentUrl(String documentUrl) {
        this.remarks = documentUrl;
    }

    public LocalDateTime getSubmittedAt() {
        return requestedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.requestedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return verifiedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.verifiedAt = reviewedAt;
    }

    public Boolean getIsDeleted() {
        return false;
    }

    public void setIsDeleted(Boolean isDeleted) {
    }
}
