package com.lms.usermanagementservice.security.permission;

import com.lms.usermanagementservice.entity.Permission;
import com.lms.usermanagementservice.entity.RolePermission;
import com.lms.usermanagementservice.entity.UserRole;
import com.lms.usermanagementservice.repository.RolePermissionRepository;
import com.lms.usermanagementservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionEvaluator {

    private final UserRoleRepository userRoleRepository;

    private final RolePermissionRepository rolePermissionRepository;

    public boolean hasPermission(Long userId, String permissionName) {

        List<UserRole> userRoles =
                userRoleRepository.findByUserId(userId);

        for (UserRole userRole : userRoles) {

            List<RolePermission> rolePermissions =
                    rolePermissionRepository.findByRoleId(
                            userRole.getRole().getId()
                    );

            for (RolePermission rolePermission : rolePermissions) {

                Permission permission =
                        rolePermission.getPermission();

                if (permission.getPermissionName()
                        .equalsIgnoreCase(permissionName)) {

                    return true;
                }
            }
        }

        return false;
    }
}