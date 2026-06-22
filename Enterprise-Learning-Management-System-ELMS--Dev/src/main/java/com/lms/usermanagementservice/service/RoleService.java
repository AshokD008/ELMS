package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.request.CreateRoleRequest;
import com.lms.usermanagementservice.dto.request.UpdateRoleRequest;
import com.lms.usermanagementservice.dto.request.AssignRoleRequest;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(
            Long roleId,
            UpdateRoleRequest request
    );

    RoleResponse getRoleById(Long roleId);

    RoleResponse getRoleByName(String roleName);

    PageResponse<RoleResponse> getAllRoles(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    PageResponse<RoleResponse> searchRoles(
            String keyword,
            int page,
            int size
    );

    RoleResponse assignRoleToUser(
            AssignRoleRequest request
    );

    RoleResponse removeRoleFromUser(
            AssignRoleRequest request
    );

    List<RoleResponse> getUserRoles(Long userId);

    boolean hasRole(
            Long userId,
            String roleName
    );

    RoleResponse activateRole(Long roleId);

    RoleResponse deactivateRole(Long roleId);

    void deleteRole(Long roleId);

    void softDeleteRole(Long roleId);
}