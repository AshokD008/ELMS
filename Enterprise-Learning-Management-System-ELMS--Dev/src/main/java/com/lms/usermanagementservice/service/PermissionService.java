package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.request.AssignPermissionRequest;
import com.lms.usermanagementservice.dto.request.CreatePermissionRequest;
import com.lms.usermanagementservice.dto.request.UpdatePermissionRequest;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse createPermission(
            CreatePermissionRequest request
    );

    PermissionResponse updatePermission(
            Long permissionId,
            UpdatePermissionRequest request
    );

    PermissionResponse getPermissionById(
            Long permissionId
    );

    PermissionResponse getPermissionByName(
            String permissionName
    );

    PageResponse<PermissionResponse> getAllPermissions(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    PageResponse<PermissionResponse> searchPermissions(
            String keyword,
            int page,
            int size
    );

    PermissionResponse assignPermissionToRole(
            AssignPermissionRequest request
    );

    PermissionResponse removePermissionFromRole(
            AssignPermissionRequest request
    );

    List<PermissionResponse> getRolePermissions(
            Long roleId
    );

    Boolean hasPermission(
            Long userId,
            String permissionName
    );

    PermissionResponse activatePermission(
            Long permissionId
    );

    PermissionResponse deactivatePermission(
            Long permissionId
    );

    void deletePermission(Long permissionId);

    void softDeletePermission(Long permissionId);
}