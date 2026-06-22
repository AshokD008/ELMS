
package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.ProfileResponse;
import com.lms.usermanagementservice.dto.response.SuperAdminResponse;
import com.lms.usermanagementservice.dto.response.TokenResponse;
import com.lms.usermanagementservice.service.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/super-admin")
@RequiredArgsConstructor
@Tag(
        name = "Super Admin Controller",
        description = "Super Admin Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    /*
     * =========================================================
     * USER APPROVAL MANAGEMENT
     * =========================================================
     */

    @Operation(
            summary = "Approve User",
            description = "Approve pending user registration"
    )
    @PatchMapping("/users/{userId}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TokenResponse>>
    approveUser(
            @PathVariable Long userId
    ) {

        TokenResponse response =
                superAdminService.approveUser(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User approved successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Reject User",
            description = "Reject pending user registration"
    )
    @PatchMapping("/users/{userId}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TokenResponse>>
    rejectUser(
            @PathVariable Long userId,
            @RequestParam String reason
    ) {

        TokenResponse response =
                superAdminService.rejectUser(
                        userId,
                        reason
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User rejected successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Pending Approval Users",
            description = "Fetch users pending approval"
    )
    @GetMapping("/users/pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<
            ApiResponse<PageResponse<ProfileResponse>>
            > getPendingApprovalUsers(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        PageResponse<ProfileResponse> response =
                superAdminService
                        .getPendingApprovalUsers(
                                page,
                                size
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Pending approval users fetched successfully",
                        response
                )
        );
    }

    /*
     * =========================================================
     * USER STATUS MANAGEMENT
     * =========================================================
     */

    @Operation(
            summary = "Activate User",
            description = "Activate user account"
    )
    @PatchMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TokenResponse>>
    activateUser(
            @PathVariable Long userId
    ) {

        TokenResponse response =
                superAdminService.activateUser(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User activated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Deactivate User",
            description = "Deactivate user account"
    )
    @PatchMapping("/users/{userId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TokenResponse>>
    deactivateUser(
            @PathVariable Long userId
    ) {

        TokenResponse response =
                superAdminService.deactivateUser(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User deactivated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Block User",
            description = "Block user account"
    )
    @PatchMapping("/users/{userId}/block")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TokenResponse>>
    blockUser(
            @PathVariable Long userId
    ) {

        TokenResponse response =
                superAdminService.blockUser(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User blocked successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Unblock User",
            description = "Unblock user account"
    )
    @PatchMapping("/users/{userId}/unblock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TokenResponse>>
    unblockUser(
            @PathVariable Long userId
    ) {

        TokenResponse response =
                superAdminService.unblockUser(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User unblocked successfully",
                        response
                )
        );
    }

    /*
     * =========================================================
     * SUPER ADMIN PROFILE
     * =========================================================
     */

    @Operation(
            summary = "Get Current Super Admin Profile",
            description = "Fetch logged-in super admin profile"
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SuperAdminResponse>>
    getCurrentSuperAdminProfile() {

        SuperAdminResponse response =
                superAdminService
                        .getCurrentSuperAdminProfile();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current super admin profile fetched successfully",
                        response
                )
        );
    }
}

