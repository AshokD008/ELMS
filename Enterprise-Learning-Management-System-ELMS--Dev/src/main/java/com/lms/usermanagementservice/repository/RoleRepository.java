package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    default boolean existsByName(String name) {
        return existsByRoleName(name);
    }
}
