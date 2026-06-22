package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.DeviceStatus;
import com.lms.usermanagementservice.enums.DeviceType;
import com.lms.usermanagementservice.enums.PlatformType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private Long id;

    private String deviceId;

    private DeviceStatus status;

    private LocalDateTime lastLogoutAt;

    private LocalDateTime createdAt;

    private String deviceUniqueId;

    private String deviceName;

    private DeviceType deviceType;

    private PlatformType platformType;

    private DeviceStatus deviceStatus;

    private String pushToken;

    private LocalDateTime lastLoginAt;
}
