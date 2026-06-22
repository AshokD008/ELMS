package com.lms.usermanagementservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "super_admin_profiles",
     
        indexes = {
                @Index(name = "idx_super_admin_user", columnList = "user_id"),
              
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuperAdminProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "designation", length = 150)
    private String designation;

    @Column(name = "department", length = 150)
    private String department;

    @Column(name = "office_mobile", length = 20)
    private String officeMobile;

    @Column(name = "office_email", length = 150)
    private String officeEmail;

    @Column(name = "access_level", length = 100)
    private String accessLevel;

    @Column(name = "can_manage_colleges", nullable = false)
    @Builder.Default
    private Boolean canManageColleges = true;

    @Column(name = "can_manage_admins", nullable = false)
    @Builder.Default
    private Boolean canManageAdmins = true;

    @Column(name = "can_manage_system_settings", nullable = false)
    @Builder.Default
    private Boolean canManageSystemSettings = true;

    @Column(name = "can_view_audit_logs", nullable = false)
    @Builder.Default
    private Boolean canViewAuditLogs = true;

    @Column(name = "profile_completed", nullable = false)
    @Builder.Default
    private Boolean profileCompleted = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
