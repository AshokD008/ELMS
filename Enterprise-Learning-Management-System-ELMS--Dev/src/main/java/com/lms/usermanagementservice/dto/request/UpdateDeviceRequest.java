package com.lms.usermanagementservice.dto.request;

import com.lms.usermanagementservice.enums.DeviceStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeviceRequest {

    @NotBlank(message = "Device name is required")
    private String deviceName;

    private DeviceStatus deviceStatus;

    private com.lms.usermanagementservice.enums.DeviceType deviceType;

    private com.lms.usermanagementservice.enums.PlatformType platformType;
}
