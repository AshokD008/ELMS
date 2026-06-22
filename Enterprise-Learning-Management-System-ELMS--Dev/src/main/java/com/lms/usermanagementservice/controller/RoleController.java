package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.request.AssignRoleRequest;
import com.lms.usermanagementservice.dto.request.CreateRoleRequest;
import com.lms.usermanagementservice.dto.request.UpdateRoleRequest;
import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.RoleResponse;
import com.lms.usermanagementservice.service.RoleService;
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
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(
        name = "Role Controller",
        description = "Role Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @Operation(
            summary = "Create Role",
            description = "Create new role"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Role created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Role already exists",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {

        RoleResponse response =
                roleService.createRole(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Role created successfully",
                                response
                        )
                );
    }

    @Operation(
            summary = "Update Role",
            description = "Update existing role"
    )
    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {

        RoleResponse response =
                roleService.updateRole(
                        roleId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Role By ID",
            description = "Fetch role by ID"
    )
    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @PathVariable Long roleId
    ) {

        RoleResponse response =
                roleService.getRoleById(roleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Role By Name",
            description = "Fetch role by role name"
    )
    @GetMapping("/name/{roleName}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(
            @PathVariable String roleName
    ) {

        RoleResponse response =
                roleService.getRoleByName(roleName);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get All Roles",
            description = "Fetch paginated roles"
    )
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {

        PageResponse<RoleResponse> response =
                roleService.getAllRoles(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Roles fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Search Roles",
            description = "Search roles using keyword"
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> searchRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<RoleResponse> response =
                roleService.searchRoles(
                        keyword,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Roles fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Assign Role To User",
            description = "Assign role to user"
    )
    @PostMapping("/assign")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> assignRoleToUser(
            @Valid @RequestBody AssignRoleRequest request
    ) {

        RoleResponse response =
                roleService.assignRoleToUser(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role assigned successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Remove Role From User",
            description = "Remove role from user"
    )
    @PostMapping("/remove")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> removeRoleFromUser(
            @Valid @RequestBody AssignRoleRequest request
    ) {

        RoleResponse response =
                roleService.removeRoleFromUser(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role removed successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get User Roles",
            description = "Fetch all roles assigned to user"
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getUserRoles(
            @PathVariable Long userId
    ) {

        List<RoleResponse> response =
                roleService.getUserRoles(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User roles fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Check User Has Role",
            description = "Check whether user has specific role"
    )
    @GetMapping("/user/{userId}/has-role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> hasRole(
            @PathVariable Long userId,
            @RequestParam String roleName
    ) {

        Boolean response =
                roleService.hasRole(
                        userId,
                        roleName
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role validation completed",
                        response
                )
        );
    }

    @Operation(
            summary = "Activate Role",
            description = "Activate role"
    )
    @PatchMapping("/{roleId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> activateRole(
            @PathVariable Long roleId
    ) {

        RoleResponse response =
                roleService.activateRole(roleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role activated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Deactivate Role",
            description = "Deactivate role"
    )
    @PatchMapping("/{roleId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> deactivateRole(
            @PathVariable Long roleId
    ) {

        RoleResponse response =
                roleService.deactivateRole(roleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role deactivated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Soft Delete Role",
            description = "Soft delete role"
    )
    @DeleteMapping("/{roleId}/soft")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteRole(
            @PathVariable Long roleId
    ) {

        roleService.softDeleteRole(roleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role soft deleted successfully"
                )
        );
    }

    @Operation(
            summary = "Delete Role",
            description = "Permanently delete role"
    )
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable Long roleId
    ) {

        roleService.deleteRole(roleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role deleted successfully"
                )
        );
    }
}