package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {

    void createAuditLog(
            Long userId,
            String action,
            String description
    );

    AuditLog getAuditLogById(Long auditLogId);

    List<AuditLog> getUserAuditLogs(Long userId);

    PageResponse<AuditLog> getAllAuditLogs(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    PageResponse<AuditLog> searchAuditLogs(
            String keyword,
            int page,
            int size
    );

    PageResponse<AuditLog> getAuditLogsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    );

    PageResponse<AuditLog> getAuditLogsByAction(
            String action,
            int page,
            int size
    );

    void deleteAuditLog(Long auditLogId);

    void clearOldAuditLogs(Integer days);

    Long getAuditLogCount();

    Long getUserAuditLogCount(Long userId);
}