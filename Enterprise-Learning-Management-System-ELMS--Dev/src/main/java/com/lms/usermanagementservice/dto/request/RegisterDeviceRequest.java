package com.lms.usermanagementservice.dto.request;

import com.lms.usermanagementservice.enums.DeviceType;
import com.lms.usermanagementservice.enums.PlatformType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeviceRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Device name is required")
    private String deviceName;

    @NotNull(message = "Device type is required")
    private DeviceType deviceType;

    @NotNull(message = "Platform type is required")
    private PlatformType platformType;

    private String pushToken;
}