package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.Verification;
import com.lms.usermanagementservice.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, Long>, JpaSpecificationExecutor<Verification> {

    Optional<Verification> findByVerificationToken(String verificationToken);

    default List<Verification> findByVerificationStatus(VerificationStatus verificationStatus) {
        return findByStatus(verificationStatus);
    }

    List<Verification> findByStatus(VerificationStatus status);

    boolean existsByUserIdAndVerificationToken(Long userId, String verificationToken);

    default boolean existsByUserIdAndDocumentNumber(Long userId, String documentNumber) {
        return existsByUserIdAndVerificationToken(userId, documentNumber);
    }

    List<Verification> findAllByUserId(Long userId);

    Optional<Verification> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndStatus(Long userId, VerificationStatus status);
}
