package com.lms.usermanagementservice.security.permission;

import com.lms.usermanagementservice.entity.Permission;
import com.lms.usermanagementservice.entity.Role;
import com.lms.usermanagementservice.entity.RolePermission;
import com.lms.usermanagementservice.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolePermissionManager {

    private final RolePermissionRepository rolePermissionRepository;

    public Set<String> getPermissionsByRole(Role role) {

        List<RolePermission> rolePermissions =
                rolePermissionRepository.findByRoleId(role.getId());

        return rolePermissions.stream()
                .map(RolePermission::getPermission)
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());
    }

    public boolean roleHasPermission(Role role,
                                     String permissionName) {

        Set<String> permissions =
                getPermissionsByRole(role);

        return permissions.contains(permissionName);
    }
}