
package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.ProfileResponse;
import com.lms.usermanagementservice.dto.response.SuperAdminResponse;
import com.lms.usermanagementservice.dto.response.TokenResponse;
import com.lms.usermanagementservice.entity.SuperAdminProfile;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.ApprovalStage;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.enums.UserStatus;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.exception.CriticalAccessException;
import com.lms.usermanagementservice.enums.AccountType;
import com.lms.usermanagementservice.mapper.UserMapper;
import com.lms.usermanagementservice.repository.SuperAdminProfileRepository;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.SuperAdminService;
import com.lms.usermanagementservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SuperAdminServiceImpl
        implements SuperAdminService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final UserRepository userRepository;

    private final SuperAdminProfileRepository
            superAdminProfileRepository;

    private final UserMapper userMapper;

    private final SecurityUtil securityUtil;

    private final AuditLogService auditLogService;

    /*
     * =========================================================
     * USER APPROVAL MANAGEMENT
     * =========================================================
     */

    @Override
    public TokenResponse approveUser(
            Long userId
    ) {

        validateRootSuperAdmin();

        User user = findUserById(userId);

        validateApprovalEligibility(user);

        user.setStatus(UserStatus.ACTIVE);

        user.setApprovalStage(
                ApprovalStage.SUPER_ADMIN_APPROVED
        );

        user.setApprovedBySuperAdmin(
                securityUtil.getCurrentUser().getId()
        );

        user.setSuperAdminApprovedAt(
                LocalDateTime.now()
        );

        user.setRejectionReason(null);

        userRepository.save(user);

        createAuditLog(
                "USER_APPROVED",
                "User approved successfully"
        );

        log.info(
                "User approved successfully. userId={}",
                userId
        );

        return TokenResponse.builder()
                .success(true)
                .message(
                        MessageConstants.USER_APPROVED_SUCCESSFULLY
                )
                .build();
    }

    @Override
    public TokenResponse rejectUser(
            Long userId,
            String reason
    ) {

        validateRootSuperAdmin();

        User user = findUserById(userId);

        validateApprovalEligibility(user);

        validateRejectionReason(reason);

        user.setStatus(UserStatus.REJECTED);

        user.setApprovalStage(
                ApprovalStage.REJECTED
        );

        user.setRejectionReason(
                reason.trim()
        );

        userRepository.save(user);

        createAuditLog(
                "USER_REJECTED",
                "User rejected successfully"
        );

        log.warn(
                "User rejected. userId={}, reason={}",
                userId,
                reason
        );

        return TokenResponse.builder()
                .success(true)
                .message(
                        MessageConstants.USER_REJECTED_SUCCESSFULLY
                )
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProfileResponse>
    getPendingApprovalUsers(
            int page,
            int size
    ) {

        validateRootSuperAdmin();

        int validatedPage =
                Math.max(page, 0);

        int validatedSize =
                size <= 0
                        ? DEFAULT_PAGE_SIZE
                        : Math.min(size, 100);

        Pageable pageable =
                PageRequest.of(
                        validatedPage,
                        validatedSize,
                        Sort.by("createdAt")
                                .descending()
                );

        Page<User> userPage =
                userRepository.findAllByStatusAndEmailVerifiedTrueAndAccountTypeInAndCollegeAdminIdIsNull(
                        UserStatus.PENDING_VERIFICATION,
                        List.of(AccountType.COLLEGE_ADMIN, AccountType.STUDENT),
                        pageable);

        List<ProfileResponse> responses =
                userPage.getContent()
                        .stream()
                        .map(userMapper::toProfileResponse)
                        .toList();

        return PageResponse
                .<ProfileResponse>builder()
                .content(responses)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalPages(
                        userPage.getTotalPages()
                )
                .totalElements(
                        userPage.getTotalElements()
                )
                .last(userPage.isLast())
                .build();
    }

    /*
     * =========================================================
     * USER STATUS MANAGEMENT
     * =========================================================
     */

    @Override
    public TokenResponse activateUser(
            Long userId
    ) {

        validateRootSuperAdmin();

        User user = findUserById(userId);

        validateSelfAction(user);

        if (user.getStatus()
                == UserStatus.ACTIVE) {

            throw new ValidationException(
                    MessageConstants.USER_ALREADY_ACTIVE
            );
        }

        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        createAuditLog(
                "USER_ACTIVATED",
                "User activated successfully"
        );

        log.info(
                "User activated successfully. userId={}",
                userId
        );

        return TokenResponse.builder()
                .success(true)
                .message(
                        MessageConstants.USER_ACTIVATED_SUCCESSFULLY
                )
                .build();
    }

    @Override
    public TokenResponse deactivateUser(
            Long userId
    ) {

        validateRootSuperAdmin();

        User user = findUserById(userId);

        validateSelfAction(user);

        if (user.getStatus() == UserStatus.REJECTED) {

            throw new ValidationException(
                    MessageConstants.USER_ALREADY_INACTIVE
            );
        }

        user.setStatus(UserStatus.REJECTED);

        userRepository.save(user);

        createAuditLog(
                "USER_DEACTIVATED",
                "User deactivated successfully"
        );

        log.warn(
                "User deactivated successfully. userId={}",
                userId
        );

        return TokenResponse.builder()
                .success(true)
                .message(
                        MessageConstants.USER_DEACTIVATED_SUCCESSFULLY
                )
                .build();
    }

    @Override
    public TokenResponse blockUser(
            Long userId
    ) {

        validateRootSuperAdmin();

        User user = findUserById(userId);

        validateSelfAction(user);

        if (Boolean.TRUE.equals(user.getAccountLocked())) {

            throw new ValidationException(
                    MessageConstants.USER_ALREADY_BLOCKED
            );
        }

        user.setAccountLocked(true);

        userRepository.save(user);

        createAuditLog(
                "USER_BLOCKED",
                "User blocked successfully"
        );

        log.warn(
                "User blocked successfully. userId={}",
                userId
        );

        return TokenResponse.builder()
                .success(true)
                .message(
                        MessageConstants.USER_BLOCKED_SUCCESSFULLY
                )
                .build();
    }

    @Override
    public TokenResponse unblockUser(
            Long userId
    ) {

        validateRootSuperAdmin();

        User user = findUserById(userId);

        if (!Boolean.TRUE.equals(user.getAccountLocked())) {

            throw new ValidationException(
                    MessageConstants.USER_NOT_BLOCKED
            );
        }

        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);

        userRepository.save(user);

        createAuditLog(
                "USER_UNBLOCKED",
                "User unblocked successfully"
        );

        log.info(
                "User unblocked successfully. userId={}",
                userId
        );

        return TokenResponse.builder()
                .success(true)
                .message(
                        MessageConstants.USER_UNBLOCKED_SUCCESSFULLY
                )
                .build();
    }

    /*
     * =========================================================
     * SUPER ADMIN PROFILE
     * =========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public SuperAdminResponse
    getCurrentSuperAdminProfile() {

        User currentUser =
                securityUtil.getCurrentUser();

        SuperAdminProfile profile =
                superAdminProfileRepository
                        .findByUserId(
                                currentUser.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.SUPER_ADMIN_NOT_FOUND
                                )
                        );

        return mapToResponse(profile);
    }

    /*
     * =========================================================
     * PRIVATE HELPER METHODS
     * =========================================================
     */

    private User findUserById(
            Long userId
    ) {

        if (userId == null || userId <= 0) {

            throw new ValidationException(
                    MessageConstants.INVALID_USER_ID
            );
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND
                        )
                );
    }

    private void validateApprovalEligibility(
            User user
    ) {

        boolean inSuperAdminScope = user.getAccountType() == AccountType.COLLEGE_ADMIN
                || (user.getAccountType() == AccountType.STUDENT && user.getCollegeAdminId() == null);
        if (!inSuperAdminScope) {
            throw new ForbiddenException("Target user is outside the SUPER_ADMIN approval scope");
        }

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new ValidationException("User is already ACTIVE");
        }

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {

            throw new ValidationException(
                    MessageConstants.INVALID_USER_STATUS
            );
        }

        if (!Boolean.TRUE.equals(
                user.getEmailVerified()
        )) {

            throw new ValidationException(
                    MessageConstants.EMAIL_NOT_VERIFIED
            );
        }
    }

    private void validateRejectionReason(
            String reason
    ) {

        if (!StringUtils.hasText(reason)) {

            throw new ValidationException(
                    MessageConstants.REJECTION_REASON_REQUIRED
            );
        }

        if (reason.length() > 500) {

            throw new ValidationException(
                    MessageConstants.REJECTION_REASON_TOO_LONG
            );
        }
    }

    private void validateSelfAction(
            User user
    ) {

        Long currentUserId =
                securityUtil.getCurrentUser()
                        .getId();

        if (user.getId().equals(currentUserId)) {

            throw new ValidationException(
                    MessageConstants.SELF_ACTION_NOT_ALLOWED
            );
        }
    }

    private void validateRootSuperAdmin() {

        User currentUser =
                securityUtil.getCurrentUser();

        boolean isSuperAdmin =
                currentUser.getUserRoles()
                        .stream()
                        .anyMatch(role ->
                                isRole(
                                        role.getRole().getName(),
                                        UserRoleType.SUPER_ADMIN
                                )
                        );

        if (!isSuperAdmin) {

            throw new CriticalAccessException(MessageConstants.ACCESS_DENIED);
        }
    }

    private boolean isRole(
            String actualRole,
            UserRoleType expectedRole
    ) {

        return expectedRole.name()
                .equals(actualRole)

                || ("ROLE_" + expectedRole.name())
                .equals(actualRole);
    }

    private void createAuditLog(
            String action,
            String description
    ) {

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                action,
                description
        );
    }

    private SuperAdminResponse mapToResponse(
            SuperAdminProfile profile
    ) {

        return SuperAdminResponse.builder()
                .id(profile.getId())
                .user(
                        userMapper.toProfileResponse(
                                profile.getUser()
                        )
                )
                .department(
                        profile.getDepartment()
                )
                .accessLevel(
                        profile.getAccessLevel()
                )
                .createdAt(
                        profile.getCreatedAt()
                )
                .build();
    }
}

