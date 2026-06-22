package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.StudentProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long>, JpaSpecificationExecutor<StudentProfile> {

    @EntityGraph(attributePaths = {"user", "college"})
    @Query("select s from StudentProfile s where s.id = :id")
    Optional<StudentProfile> findWithUserAndCollegeById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "college"})
    Optional<StudentProfile> findByUserId(Long userId);

    default boolean existsByStudentRollNumber(String studentRollNumber) {
        return existsByRollNumber(studentRollNumber);
    }

    boolean existsByRollNumber(String rollNumber);

    long countByCollegeId(Long collegeId);

    long countByStudentIdStartingWith(String prefix);

    @EntityGraph(attributePaths = {"user", "college"})
    List<StudentProfile> findAllByCollegeId(Long collegeId);

    Page<StudentProfile> findAllByUserCollegeAdminId(Long collegeAdminId, Pageable pageable);

    ///List<StudentProfile> findAllByCollegeIdAndUserCollegeAdminId(Long collegeId, Long collegeAdminId);
    List<StudentProfile> findAllByCollegeIdAndUserCollegeAdmin_Id(
            Long collegeId,
            Long collegeAdminId);
}
