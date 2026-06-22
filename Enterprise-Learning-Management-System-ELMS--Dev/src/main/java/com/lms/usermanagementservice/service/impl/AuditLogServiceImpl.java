package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.entity.AuditLog;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.repository.AuditLogRepository;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.util.SecurityUtil;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final String AUDIT_LOG_CACHE_PREFIX =
            "AUDIT_LOG:";

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    private final SecurityUtil securityUtil;

    private final HttpServletRequest httpServletRequest;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public void createAuditLog(
            Long userId,
            String action,
            String description
    ) {

        try {

            User user = null;

            if (userId != null) {

                user = userRepository.findById(userId)
                        .orElse(null);
            }

            AuditLog auditLog = new AuditLog();

            auditLog.setUser(user);
            auditLog.setAction(action);
            auditLog.setDescription(description);

            // ADD THESE
            auditLog.setModuleName("AUTH_MODULE");
            auditLog.setIsSuccess(true);

            auditLog.setIpAddress(
                    httpServletRequest.getRemoteAddr()
            );

            auditLog.setUserAgent(
                    httpServletRequest.getHeader("User-Agent")
            );

            auditLog.setCreatedAt(LocalDateTime.now());

            AuditLog savedLog =
                    auditLogRepository.save(auditLog);

            cacheAuditLog(savedLog);

            log.info(
                    "{} : {}",
                    LogConstants.AUDIT_LOG_CREATED,
                    action
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to create audit log: {}",
                    ex.getMessage()
            );
        }
    }
    @Override
    public AuditLog getAuditLogById(
            Long auditLogId
    ) {

        validateAdminAccess();

        return auditLogRepository.findById(auditLogId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.AUDIT_LOG_NOT_FOUND
                        ));
    }

    @Override
    public List<AuditLog> getUserAuditLogs(
            Long userId
    ) {

        User currentUser =
                securityUtil.getCurrentUser();

        validateAuditAccess(currentUser, userId);

        return auditLogRepository
                .findAllByUserIdOrderByCreatedAtDesc(
                        userId
                );
    }

    @Override
    public PageResponse<AuditLog> getAllAuditLogs(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {

        validateAdminAccess();

        Sort sort =
                sortDirection.equalsIgnoreCase("DESC")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Page<AuditLog> auditLogPage =
                auditLogRepository.findAll(pageable);

        return PageResponse.<AuditLog>builder()
                .content(auditLogPage.getContent())
                .page(page)
                .size(size)
                .totalPages(auditLogPage.getTotalPages())
                .totalElements(auditLogPage.getTotalElements())
                .last(auditLogPage.isLast())
                .build();
    }

    @Override
    public PageResponse<AuditLog> searchAuditLogs(
            String keyword,
            int page,
            int size
    ) {

        validateAdminAccess();

        Pageable pageable =
                PageRequest.of(page, size);

        Specification<AuditLog> specification =
                (root, query, criteriaBuilder) -> {

                    List<Predicate> predicates =
                            new ArrayList<>();

                    if (StringUtils.hasText(keyword)) {

                        predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("action")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("description")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("ipAddress")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        )
                                )
                        );
                    }

                    return criteriaBuilder.and(
                            predicates.toArray(new Predicate[0])
                    );
                };

        Page<AuditLog> auditLogPage =
                auditLogRepository.findAll(
                        specification,
                        pageable
                );

        return PageResponse.<AuditLog>builder()
                .content(auditLogPage.getContent())
                .page(page)
                .size(size)
                .totalPages(auditLogPage.getTotalPages())
                .totalElements(auditLogPage.getTotalElements())
                .last(auditLogPage.isLast())
                .build();
    }

    @Override
    public PageResponse<AuditLog> getAuditLogsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    ) {

        validateAdminAccess();

        if (startDate == null || endDate == null) {

            throw new ValidationException(
                    MessageConstants.INVALID_DATE_RANGE
            );
        }

        Pageable pageable =
                PageRequest.of(page, size);

        Page<AuditLog> auditLogPage =
                auditLogRepository
                        .findAllByCreatedAtBetween(
                                startDate,
                                endDate,
                                pageable
                        );

        return PageResponse.<AuditLog>builder()
                .content(auditLogPage.getContent())
                .page(page)
                .size(size)
                .totalPages(auditLogPage.getTotalPages())
                .totalElements(auditLogPage.getTotalElements())
                .last(auditLogPage.isLast())
                .build();
    }

    @Override
    public PageResponse<AuditLog> getAuditLogsByAction(
            String action,
            int page,
            int size
    ) {

        validateAdminAccess();

        Pageable pageable =
                PageRequest.of(page, size);

        Page<AuditLog> auditLogPage =
                auditLogRepository
                        .findAllByActionContainingIgnoreCase(
                                action,
                                pageable
                        );

        return PageResponse.<AuditLog>builder()
                .content(auditLogPage.getContent())
                .page(page)
                .size(size)
                .totalPages(auditLogPage.getTotalPages())
                .totalElements(auditLogPage.getTotalElements())
                .last(auditLogPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void deleteAuditLog(
            Long auditLogId
    ) {

        validateAdminAccess();

        AuditLog auditLog =
                auditLogRepository.findById(auditLogId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.AUDIT_LOG_NOT_FOUND
                                ));

        auditLogRepository.delete(auditLog);

        redisTemplate.delete(
                AUDIT_LOG_CACHE_PREFIX + auditLogId
        );

        log.info(
                "Audit log deleted: {}",
                auditLogId
        );
    }

    @Override
    @Transactional
    public void clearOldAuditLogs(
            Integer days
    ) {

        validateAdminAccess();

        if (days == null || days <= 0) {

            throw new ValidationException(
                    MessageConstants.INVALID_DAYS
            );
        }

        LocalDateTime cutoffDate =
                LocalDateTime.now().minusDays(days);

        List<AuditLog> oldLogs =
                auditLogRepository
                        .findAllByCreatedAtBefore(
                                cutoffDate
                        );

        if (!oldLogs.isEmpty()) {

            oldLogs.forEach(log ->
                    redisTemplate.delete(
                            AUDIT_LOG_CACHE_PREFIX
                                    + log.getId()
                    )
            );

            auditLogRepository.deleteAll(oldLogs);
        }

        log.info(
                "Old audit logs cleared successfully"
        );
    }

    @Override
    public Long getAuditLogCount() {

        validateAdminAccess();

        return auditLogRepository.count();
    }

    @Override
    public Long getUserAuditLogCount(
            Long userId
    ) {

        User currentUser =
                securityUtil.getCurrentUser();

        validateAuditAccess(currentUser, userId);

        return auditLogRepository.countByUserId(
                userId
        );
    }

    private void validateAdminAccess() {

        User currentUser =
                securityUtil.getCurrentUser();

        boolean isAdmin =
                currentUser.getUserRoles()
                        .stream()
                        .anyMatch(role ->
                                role.getRole()
                                        .getName()
                                        .equals(
                                                UserRoleType.SUPER_ADMIN.name()
                                        )
                        );

        if (!isAdmin) {

            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }
    }

    private void validateAuditAccess(
            User currentUser,
            Long userId
    ) {

        boolean isAdmin =
                currentUser.getUserRoles()
                        .stream()
                        .anyMatch(role ->
                                role.getRole()
                                        .getName()
                                        .equals(
                                                UserRoleType.SUPER_ADMIN.name()
                                        )
                        );

        if (!isAdmin
                && !currentUser.getId().equals(userId)) {

            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }
    }

    private void cacheAuditLog(
            AuditLog auditLog
    ) {

        HashMap<String, Object> cacheValue = new HashMap<>();
        cacheValue.put("id", auditLog.getId());
        cacheValue.put("userId", auditLog.getUser() == null ? null : auditLog.getUser().getId());
        cacheValue.put("action", auditLog.getAction());
        cacheValue.put("description", auditLog.getDescription());
        cacheValue.put("moduleName", auditLog.getModuleName());
        cacheValue.put("isSuccess", auditLog.getIsSuccess());

        redisTemplate.opsForValue().set(
                AUDIT_LOG_CACHE_PREFIX
                        + auditLog.getId(),
                cacheValue,
                Duration.ofHours(24)
        );
    }
}
