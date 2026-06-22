package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.FacultyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, Long> {
    Optional<FacultyProfile> findByUserId(Long userId);
   // long countByUserCollegeAdminId(Long collegeAdminId);
    long countByUserCollegeAdmin_Id(Long collegeAdminId);
    //List<FacultyProfile> findAllByUserCollegeAdminId(Long collegeAdminId);
    List<FacultyProfile> findAllByUserCollegeAdmin_Id(Long collegeAdminId);
}
