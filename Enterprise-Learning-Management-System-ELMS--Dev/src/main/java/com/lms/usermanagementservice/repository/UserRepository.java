package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.UserStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"college", "userRoles", "userRoles.role"})
    @Query("select u from User u where u.email = :email")
    Optional<User> findWithRolesAndCollegeByEmail(@Param("email") String email);

    @EntityGraph(attributePaths = {"college", "userRoles", "userRoles.role"})
    @Query("select u from User u where u.id = :id")
    Optional<User> findWithRolesAndCollegeById(@Param("id") Long id);

	Page<User> findAllByStatus(UserStatus pendingApproval, Pageable pageable);

    Page<User> findAllByStatusAndEmailVerifiedTrueAndAccountTypeInAndCollegeAdminIdIsNull(
            UserStatus status, List<com.lms.usermanagementservice.enums.AccountType> accountTypes, Pageable pageable);

//    Page<User> findAllByStatusAndEmailVerifiedTrueAndCollegeAdminId(
//            UserStatus status, Long collegeAdminId, Pageable pageable);
    Page<User> findAllByStatusAndEmailVerifiedTrueAndCollegeAdmin_Id(
            UserStatus status,
            Long collegeAdminId,
            Pageable pageable);
//    List<User> findAllByCollegeAdminId(Long collegeAdminId);
    List<User> findAllByCollegeAdmin_Id(Long collegeAdminId);
}
