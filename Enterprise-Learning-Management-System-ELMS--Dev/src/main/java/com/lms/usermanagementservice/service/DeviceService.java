package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.request.RegisterDeviceRequest;
import com.lms.usermanagementservice.dto.request.UpdateDeviceRequest;
import com.lms.usermanagementservice.dto.request.UpdatePushTokenRequest;
import com.lms.usermanagementservice.dto.response.DeviceResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;

import java.util.List;

public interface DeviceService {

    DeviceResponse registerDevice(
            RegisterDeviceRequest request
    );

    DeviceResponse updateDevice(
            Long deviceId,
            UpdateDeviceRequest request
    );

    DeviceResponse updatePushToken(
            Long deviceId,
            UpdatePushTokenRequest request
    );

    DeviceResponse getDeviceById(Long deviceId);

    List<DeviceResponse> getCurrentUserDevices();

    PageResponse<DeviceResponse> getAllDevices(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    PageResponse<DeviceResponse> searchDevices(
            String keyword,
            int page,
            int size
    );

    DeviceResponse activateDevice(Long deviceId);

    DeviceResponse deactivateDevice(Long deviceId);

    void removeDevice(Long deviceId);

    void logoutDevice(Long deviceId);

    void logoutAllDevices(Long userId);

    Boolean isDeviceRegistered(
            String deviceId,
            Long userId
    );
}