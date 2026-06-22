package com.lms.usermanagementservice.entity;

import com.lms.usermanagementservice.enums.PermissionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "permission_name"),
                @UniqueConstraint(columnNames = "permission_code")
        },
        indexes = {
                @Index(name = "idx_permission_name", columnList = "permission_name"),
                @Index(name = "idx_permission_code", columnList = "permission_code"),
                @Index(name = "idx_permission_type", columnList = "permission_type"),
                @Index(name = "idx_permission_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_name", nullable = false, unique = true, length = 150)
    private String permissionName;

    @Column(name = "permission_code", nullable = false, unique = true, length = 150)
    private String permissionCode;

    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false, length = 50)
    private PermissionType permissionType;

    @Column(name = "is_system_permission", nullable = false)
    @Builder.Default
    private Boolean isSystemPermission = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder;

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

    public String getName() {
        return permissionName;
    }

    public void setName(String name) {
        this.permissionName = name;
    }

    public Boolean getEnabled() {
        return isActive;
    }

    public void setEnabled(Boolean enabled) {
        this.isActive = enabled;
    }

    public Boolean getIsDeleted() {
        return false;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isActive = !Boolean.TRUE.equals(isDeleted);
    }
}
