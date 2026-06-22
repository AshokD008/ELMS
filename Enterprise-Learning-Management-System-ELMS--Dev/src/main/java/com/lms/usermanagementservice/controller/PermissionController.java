package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.request.AssignPermissionRequest;
import com.lms.usermanagementservice.dto.request.CreatePermissionRequest;
import com.lms.usermanagementservice.dto.request.UpdatePermissionRequest;
import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.PermissionResponse;
import com.lms.usermanagementservice.service.PermissionService;
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
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(
        name = "Permission Controller",
        description = "Permission Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(
            summary = "Create Permission",
            description = "Create new permission"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Permission created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Permission already exists",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {

        PermissionResponse response =
                permissionService.createPermission(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Permission created successfully",
                                response
                        )
                );
    }

    @Operation(
            summary = "Update Permission",
            description = "Update existing permission"
    )
    @PutMapping("/{permissionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long permissionId,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {

        PermissionResponse response =
                permissionService.updatePermission(
                        permissionId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Permission By ID",
            description = "Fetch permission by ID"
    )
    @GetMapping("/{permissionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(
            @PathVariable Long permissionId
    ) {

        PermissionResponse response =
                permissionService.getPermissionById(permissionId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Permission By Name",
            description = "Fetch permission by name"
    )
    @GetMapping("/name/{permissionName}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionByName(
            @PathVariable String permissionName
    ) {

        PermissionResponse response =
                permissionService.getPermissionByName(
                        permissionName
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get All Permissions",
            description = "Fetch paginated permissions"
    )
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> getAllPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {

        PageResponse<PermissionResponse> response =
                permissionService.getAllPermissions(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permissions fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Search Permissions",
            description = "Search permissions using keyword"
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> searchPermissions(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<PermissionResponse> response =
                permissionService.searchPermissions(
                        keyword,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permissions fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Assign Permission To Role",
            description = "Assign permission to role"
    )
    @PostMapping("/assign")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> assignPermissionToRole(
            @Valid @RequestBody AssignPermissionRequest request
    ) {

        PermissionResponse response =
                permissionService.assignPermissionToRole(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission assigned successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Remove Permission From Role",
            description = "Remove permission from role"
    )
    @PostMapping("/remove")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> removePermissionFromRole(
            @Valid @RequestBody AssignPermissionRequest request
    ) {

        PermissionResponse response =
                permissionService.removePermissionFromRole(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission removed successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Role Permissions",
            description = "Fetch all permissions assigned to role"
    )
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getRolePermissions(
            @PathVariable Long roleId
    ) {

        List<PermissionResponse> response =
                permissionService.getRolePermissions(roleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role permissions fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Check User Permission",
            description = "Check whether user has specific permission"
    )
    @GetMapping("/user/{userId}/has-permission")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> hasPermission(
            @PathVariable Long userId,
            @RequestParam String permissionName
    ) {

        Boolean response =
                permissionService.hasPermission(
                        userId,
                        permissionName
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission validation completed",
                        response
                )
        );
    }

    @Operation(
            summary = "Activate Permission",
            description = "Activate permission"
    )
    @PatchMapping("/{permissionId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> activatePermission(
            @PathVariable Long permissionId
    ) {

        PermissionResponse response =
                permissionService.activatePermission(
                        permissionId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission activated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Deactivate Permission",
            description = "Deactivate permission"
    )
    @PatchMapping("/{permissionId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> deactivatePermission(
            @PathVariable Long permissionId
    ) {

        PermissionResponse response =
                permissionService.deactivatePermission(
                        permissionId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission deactivated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Soft Delete Permission",
            description = "Soft delete permission"
    )
    @DeleteMapping("/{permissionId}/soft")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeletePermission(
            @PathVariable Long permissionId
    ) {

        permissionService.softDeletePermission(
                permissionId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission soft deleted successfully"
                )
        );
    }

    @Operation(
            summary = "Delete Permission",
            description = "Permanently delete permission"
    )
    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(
            @PathVariable Long permissionId
    ) {

        permissionService.deletePermission(
                permissionId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission deleted successfully"
                )
        );
    }
}