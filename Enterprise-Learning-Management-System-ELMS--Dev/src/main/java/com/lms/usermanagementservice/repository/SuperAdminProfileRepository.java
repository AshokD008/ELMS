package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.SuperAdminProfile;
import com.lms.usermanagementservice.enums.UserStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SuperAdminProfileRepository extends JpaRepository<SuperAdminProfile, Long>, JpaSpecificationExecutor<SuperAdminProfile> {

    @EntityGraph(attributePaths = {"user"})
    @Query("select s from SuperAdminProfile s where s.id = :id")
    Optional<SuperAdminProfile> findWithUserById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<SuperAdminProfile> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<SuperAdminProfile> findAllByUserStatus(UserStatus userStatus);
}
