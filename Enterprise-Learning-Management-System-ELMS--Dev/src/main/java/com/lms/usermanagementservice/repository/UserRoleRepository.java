package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);
    long countByRole_RoleName(String roleName);
    default List<UserRole> findAllByUserId(Long userId) {
        return findByUserId(userId);
    }

    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    boolean existsByUserIdAndRoleRoleName(Long userId, String roleName);

    default boolean existsByUserIdAndRoleName(Long userId, String roleName) {
        return existsByUserIdAndRoleRoleName(userId, roleName);
    }

    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
