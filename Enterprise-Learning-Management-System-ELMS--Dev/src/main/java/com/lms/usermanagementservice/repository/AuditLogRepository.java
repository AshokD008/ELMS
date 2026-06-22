package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Page<AuditLog> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<AuditLog> findAllByActionContainingIgnoreCase(String action, Pageable pageable);

    List<AuditLog> findAllByCreatedAtBefore(LocalDateTime createdAt);

    long countByUserId(Long userId);
}
