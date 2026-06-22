package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.CollegeAdminProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollegeAdminProfileRepository extends JpaRepository<CollegeAdminProfile, Long>, JpaSpecificationExecutor<CollegeAdminProfile> {

    @EntityGraph(attributePaths = {"user", "college"})
    @Query("select c from CollegeAdminProfile c where c.id = :id")
    Optional<CollegeAdminProfile> findWithUserAndCollegeById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "college"})
    Optional<CollegeAdminProfile> findByUserId(Long userId);

    long countByCollegeId(Long collegeId);

    long countByEmployeeIdStartingWith(String prefix);

    @EntityGraph(attributePaths = {"user", "college"})
    List<CollegeAdminProfile> findAllByCollegeId(Long collegeId);
}
