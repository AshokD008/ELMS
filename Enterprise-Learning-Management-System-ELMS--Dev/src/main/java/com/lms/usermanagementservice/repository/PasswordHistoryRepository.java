package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
