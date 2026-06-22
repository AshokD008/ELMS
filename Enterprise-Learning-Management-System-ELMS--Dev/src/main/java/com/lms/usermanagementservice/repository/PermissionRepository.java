package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.Permission;
import com.lms.usermanagementservice.enums.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    Optional<Permission> findByPermissionName(String permissionName);

    List<Permission> findByPermissionType(PermissionType permissionType);

    boolean existsByPermissionName(String permissionName);

    default boolean existsByName(String name) {
        return existsByPermissionName(name);
    }
}
