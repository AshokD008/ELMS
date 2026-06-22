package com.lms.usermanagementservice.entity;

import com.lms.usermanagementservice.enums.DeviceStatus;
import com.lms.usermanagementservice.enums.DeviceType;
import com.lms.usermanagementservice.enums.PlatformType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_devices",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "device_unique_id")
        },
        indexes = {
                @Index(name = "idx_user_device_user", columnList = "user_id"),
                @Index(name = "idx_user_device_unique", columnList = "device_unique_id"),
                @Index(name = "idx_user_device_status", columnList = "device_status"),
                @Index(name = "idx_user_device_platform", columnList = "platform_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_unique_id", nullable = false, unique = true, length = 255)
    private String deviceUniqueId;

    @Column(name = "device_name", nullable = false, length = 150)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 50)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type", nullable = false, length = 50)
    private PlatformType platformType;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_status", nullable = false, length = 30)
    private DeviceStatus deviceStatus;

    @Column(name = "device_model", length = 150)
    private String deviceModel;

    @Column(name = "device_brand", length = 100)
    private String deviceBrand;

    @Column(name = "os_version", length = 100)
    private String osVersion;

    @Column(name = "app_version", length = 100)
    private String appVersion;

    @Column(name = "push_token", columnDefinition = "TEXT")
    private String pushToken;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "is_trusted", nullable = false)
    @Builder.Default
    private Boolean isTrusted = false;

    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private Boolean isBlocked = false;

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

        if (this.lastActiveAt == null) {
            this.lastActiveAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getDeviceId() {
        return deviceUniqueId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceUniqueId = deviceId;
    }

    public DeviceStatus getStatus() {
        return deviceStatus;
    }

    public void setStatus(DeviceStatus status) {
        this.deviceStatus = status;
    }

    public LocalDateTime getLastLogoutAt() {
        return lastActiveAt;
    }

    public void setLastLogoutAt(LocalDateTime lastLogoutAt) {
        this.lastActiveAt = lastLogoutAt;
    }
}
