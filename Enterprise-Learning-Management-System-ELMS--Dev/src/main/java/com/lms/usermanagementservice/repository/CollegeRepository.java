package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.College;
import com.lms.usermanagementservice.enums.CollegeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CollegeRepository extends JpaRepository<College, Long>, JpaSpecificationExecutor<College> {

    Optional<College> findByCollegeCode(String collegeCode);

    boolean existsByCollegeCode(String collegeCode);

    long countByCollegeCodeStartingWith(String prefix);

    boolean existsByEmail(String email);

    List<College> findByStatus(CollegeStatus status);

    default List<College> findAllByStatus(CollegeStatus status) {
        return findByStatus(status);
    }
}
