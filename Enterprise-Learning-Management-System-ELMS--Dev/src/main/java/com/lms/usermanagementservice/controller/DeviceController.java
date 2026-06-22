package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.request.RegisterDeviceRequest;
import com.lms.usermanagementservice.dto.request.UpdateDeviceRequest;
import com.lms.usermanagementservice.dto.request.UpdatePushTokenRequest;
import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.DeviceResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(
        name = "Device Controller",
        description = "Device Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(
            summary = "Register Device",
            description = "Register user device"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Device registered successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Device already registered",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody RegisterDeviceRequest request
    ) {

        DeviceResponse response =
                deviceService.registerDevice(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Device registered successfully",
                                response
                        )
                );
    }

    @Operation(
            summary = "Update Device",
            description = "Update registered device"
    )
    @PutMapping("/{deviceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDevice(
            @PathVariable Long deviceId,
            @Valid @RequestBody UpdateDeviceRequest request
    ) {

        DeviceResponse response =
                deviceService.updateDevice(
                        deviceId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Update Push Token",
            description = "Update device push notification token"
    )
    @PatchMapping("/{deviceId}/push-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeviceResponse>> updatePushToken(
            @PathVariable Long deviceId,
            @Valid @RequestBody UpdatePushTokenRequest request
    ) {

        DeviceResponse response =
                deviceService.updatePushToken(
                        deviceId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Push token updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Device By ID",
            description = "Fetch device by ID"
    )
    @GetMapping("/{deviceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceById(
            @PathVariable Long deviceId
    ) {

        DeviceResponse response =
                deviceService.getDeviceById(
                        deviceId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Current User Devices",
            description = "Fetch current logged-in user devices"
    )
    @GetMapping("/my-devices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getCurrentUserDevices() {

        List<DeviceResponse> response =
                deviceService.getCurrentUserDevices();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Devices fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get All Devices",
            description = "Fetch paginated devices"
    )
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<DeviceResponse>>> getAllDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {

        PageResponse<DeviceResponse> response =
                deviceService.getAllDevices(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Devices fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Search Devices",
            description = "Search devices using keyword"
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<DeviceResponse>>> searchDevices(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<DeviceResponse> response =
                deviceService.searchDevices(
                        keyword,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Devices fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Activate Device",
            description = "Activate device"
    )
    @PatchMapping("/{deviceId}/activate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeviceResponse>> activateDevice(
            @PathVariable Long deviceId
    ) {

        DeviceResponse response =
                deviceService.activateDevice(
                        deviceId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device activated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Deactivate Device",
            description = "Deactivate device"
    )
    @PatchMapping("/{deviceId}/deactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeviceResponse>> deactivateDevice(
            @PathVariable Long deviceId
    ) {

        DeviceResponse response =
                deviceService.deactivateDevice(
                        deviceId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device deactivated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Remove Device",
            description = "Remove registered device"
    )
    @DeleteMapping("/{deviceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeDevice(
            @PathVariable Long deviceId
    ) {

        deviceService.removeDevice(
                deviceId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device removed successfully"
                )
        );
    }

    @Operation(
            summary = "Logout Device",
            description = "Logout specific device"
    )
    @PatchMapping("/{deviceId}/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutDevice(
            @PathVariable Long deviceId
    ) {

        deviceService.logoutDevice(
                deviceId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device logout successful"
                )
        );
    }

    @Operation(
            summary = "Logout All Devices",
            description = "Logout all user devices"
    )
    @PatchMapping("/logout-all/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(
            @PathVariable Long userId
    ) {

        deviceService.logoutAllDevices(
                userId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All devices logged out successfully"
                )
        );
    }

    @Operation(
            summary = "Check Device Registered",
            description = "Validate whether device is registered"
    )
    @GetMapping("/registered")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> isDeviceRegistered(
            @RequestParam String deviceId,
            @RequestParam Long userId
    ) {

        Boolean response =
                deviceService.isDeviceRegistered(
                        deviceId,
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device registration validation completed",
                        response
                )
        );
    }
}