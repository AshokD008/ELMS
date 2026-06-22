package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleId(Long roleId);

    default List<RolePermission> findAllByRoleId(Long roleId) {
        return findByRoleId(roleId);
    }

    Optional<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    @Query("""
            select count(rp) > 0
            from RolePermission rp
            join UserRole ur on ur.role = rp.role
            where ur.user.id = :userId
              and rp.permission.permissionName = :permissionName
              and rp.isActive = true
            """)
    boolean existsByRoleUserRolesUserIdAndPermissionPermissionName(
            @Param("userId") Long userId,
            @Param("permissionName") String permissionName
    );

    default boolean existsByUserIdAndPermissionName(Long userId, String permissionName) {
        return existsByRoleUserRolesUserIdAndPermissionPermissionName(userId, permissionName);
    }

    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);
}
