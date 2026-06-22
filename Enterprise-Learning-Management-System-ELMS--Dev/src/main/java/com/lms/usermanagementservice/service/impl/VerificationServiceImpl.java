package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.entity.Verification;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.enums.VerificationStatus;
import com.lms.usermanagementservice.exception.DuplicateResourceException;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.repository.VerificationRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.VerificationService;
import com.lms.usermanagementservice.util.SecurityUtil;
import jakarta.persistence.criteria.Predicate;
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
public class VerificationServiceImpl
        implements VerificationService {

    private static final String VERIFICATION_CACHE_PREFIX =
            "VERIFICATION:";

    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;

    private final SecurityUtil securityUtil;

    private final AuditLogService auditLogService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public Verification createVerification(
            Long userId,
            String documentType,
            String documentNumber,
            String documentUrl
    ) {

        log.info(LogConstants.VERIFICATION_CREATE_INITIATED);

        validateVerificationRequest(
                documentType,
                documentNumber,
                documentUrl
        );

        User currentUser =
                securityUtil.getCurrentUser();

        validateVerificationAccess(
                currentUser,
                userId
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND
                        ));

        boolean verificationExists =
                verificationRepository
                        .existsByUserIdAndDocumentNumber(
                                userId,
                                documentNumber
                        );

        if (verificationExists) {

            throw new DuplicateResourceException(
                    MessageConstants.VERIFICATION_ALREADY_EXISTS
            );
        }

        Verification verification =
                new Verification();

        verification.setUser(user);
        verification.setDocumentType(documentType);
        verification.setDocumentNumber(documentNumber);
        verification.setDocumentUrl(documentUrl);
        verification.setStatus(
                VerificationStatus.PENDING
        );
        verification.setSubmittedAt(
                LocalDateTime.now()
        );
        verification.setIsDeleted(false);

        Verification savedVerification =
                verificationRepository.save(verification);

        cacheVerification(savedVerification);

        auditLogService.createAuditLog(
                userId,
                "VERIFICATION_CREATED",
                "Verification submitted successfully"
        );

        log.info(LogConstants.VERIFICATION_CREATE_SUCCESS);

        return savedVerification;
    }

    @Override
    public Verification getVerificationById(
            Long verificationId
    ) {

        Verification verification =
                verificationRepository.findById(
                                verificationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.VERIFICATION_NOT_FOUND
                                ));

        User currentUser =
                securityUtil.getCurrentUser();

        validateVerificationAccess(
                currentUser,
                verification.getUser().getId()
        );

        return verification;
    }

    @Override
    public List<Verification> getUserVerifications(
            Long userId
    ) {

        User currentUser =
                securityUtil.getCurrentUser();

        validateVerificationAccess(
                currentUser,
                userId
        );

        return verificationRepository
                .findAllByUserId(userId);
    }

    @Override
    public PageResponse<Verification>
    getAllVerifications(
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

        Page<Verification> verificationPage =
                verificationRepository.findAll(pageable);

        return PageResponse.<Verification>builder()
                .content(verificationPage.getContent())
                .page(page)
                .size(size)
                .totalPages(
                        verificationPage.getTotalPages()
                )
                .totalElements(
                        verificationPage.getTotalElements()
                )
                .last(verificationPage.isLast())
                .build();
    }

    @Override
    public PageResponse<Verification>
    searchVerifications(
            String keyword,
            int page,
            int size
    ) {

        validateAdminAccess();

        Pageable pageable =
                PageRequest.of(page, size);

        Specification<Verification> specification =
                (root, query, criteriaBuilder) -> {

                    List<Predicate> predicates =
                            new ArrayList<>();

                    if (StringUtils.hasText(keyword)) {

                        predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("documentType")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("documentNumber")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("status")
                                                                .as(String.class)
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

        Page<Verification> verificationPage =
                verificationRepository.findAll(
                        specification,
                        pageable
                );

        return PageResponse.<Verification>builder()
                .content(verificationPage.getContent())
                .page(page)
                .size(size)
                .totalPages(
                        verificationPage.getTotalPages()
                )
                .totalElements(
                        verificationPage.getTotalElements()
                )
                .last(verificationPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public Verification approveVerification(
            Long verificationId,
            String remarks
    ) {

        validateAdminAccess();

        Verification verification =
                verificationRepository.findById(
                                verificationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.VERIFICATION_NOT_FOUND
                                ));

        verification.setStatus(
                VerificationStatus.APPROVED
        );
        verification.setReviewedAt(
                LocalDateTime.now()
        );
        verification.setRemarks(remarks);

        Verification updatedVerification =
                verificationRepository.save(verification);

        cacheVerification(updatedVerification);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "VERIFICATION_APPROVED",
                "Verification approved"
        );

        return updatedVerification;
    }

    @Override
    @Transactional
    public Verification rejectVerification(
            Long verificationId,
            String remarks
    ) {

        validateAdminAccess();

        Verification verification =
                verificationRepository.findById(
                                verificationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.VERIFICATION_NOT_FOUND
                                ));

        verification.setStatus(
                VerificationStatus.REJECTED
        );
        verification.setReviewedAt(
                LocalDateTime.now()
        );
        verification.setRemarks(remarks);

        Verification updatedVerification =
                verificationRepository.save(verification);

        cacheVerification(updatedVerification);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "VERIFICATION_REJECTED",
                "Verification rejected"
        );

        return updatedVerification;
    }

    @Override
    @Transactional
    public Verification markVerificationUnderReview(
            Long verificationId,
            String remarks
    ) {

        validateAdminAccess();

        Verification verification =
                verificationRepository.findById(
                                verificationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.VERIFICATION_NOT_FOUND
                                ));

        verification.setStatus(
                VerificationStatus.UNDER_REVIEW
        );
        verification.setRemarks(remarks);

        Verification updatedVerification =
                verificationRepository.save(verification);

        cacheVerification(updatedVerification);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "VERIFICATION_UNDER_REVIEW",
                "Verification marked under review"
        );

        return updatedVerification;
    }

    @Override
    public VerificationStatus getVerificationStatus(
            Long userId
    ) {

        Verification verification =
                verificationRepository
                        .findTopByUserIdOrderByCreatedAtDesc(
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.VERIFICATION_NOT_FOUND
                                ));

        return verification.getStatus();
    }

    @Override
    public Boolean isUserVerified(
            Long userId
    ) {

        return verificationRepository
                .existsByUserIdAndStatus(
                        userId,
                        VerificationStatus.APPROVED
                );
    }

    @Override
    @Transactional
    public void deleteVerification(
            Long verificationId
    ) {

        validateAdminAccess();

        Verification verification =
                verificationRepository.findById(
                                verificationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.VERIFICATION_NOT_FOUND
                                ));

        verificationRepository.delete(verification);

        redisTemplate.delete(
                VERIFICATION_CACHE_PREFIX
                        + verificationId
        );

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "VERIFICATION_DELETED",
                "Verification deleted permanently"
        );
    }

    @Override
    @Transactional
    public void softDeleteVerification(
            Long verificationId
    ) {

        validateAdminAccess();

        Verification verification =
                verificationRepository.findById(
                                verificationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.VERIFICATION_NOT_FOUND
                                ));

        verification.setIsDeleted(true);

        verificationRepository.save(verification);

        redisTemplate.delete(
                VERIFICATION_CACHE_PREFIX
                        + verificationId
        );

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "VERIFICATION_SOFT_DELETED",
                "Verification soft deleted"
        );
    }

    private void validateVerificationRequest(
            String documentType,
            String documentNumber,
            String documentUrl
    ) {

        if (!StringUtils.hasText(documentType)) {

            throw new ValidationException(
                    MessageConstants.DOCUMENT_TYPE_REQUIRED
            );
        }

        if (!StringUtils.hasText(documentNumber)) {

            throw new ValidationException(
                    MessageConstants.DOCUMENT_NUMBER_REQUIRED
            );
        }

        if (!StringUtils.hasText(documentUrl)) {

            throw new ValidationException(
                    MessageConstants.DOCUMENT_URL_REQUIRED
            );
        }
    }

    private void validateVerificationAccess(
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

    private void cacheVerification(
            Verification verification
    ) {

        HashMap<String, Object> cacheValue = new HashMap<>();
        cacheValue.put("id", verification.getId());
        cacheValue.put("userId", verification.getUser() == null ? null : verification.getUser().getId());
        cacheValue.put("verificationType", verification.getVerificationType());
        cacheValue.put("status", verification.getStatus());
        cacheValue.put("email", verification.getEmail());
        cacheValue.put("requestedAt", verification.getRequestedAt());
        cacheValue.put("verifiedAt", verification.getVerifiedAt());

        redisTemplate.opsForValue().set(
                VERIFICATION_CACHE_PREFIX
                        + verification.getId(),
                cacheValue,
                Duration.ofHours(12)
        );
    }
}
