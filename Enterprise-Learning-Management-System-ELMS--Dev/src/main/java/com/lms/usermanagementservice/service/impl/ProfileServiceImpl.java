package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.request.UpdateProfileRequest;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.ProfileResponse;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.enums.UserStatus;
import com.lms.usermanagementservice.exception.DuplicateResourceException;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.mapper.UserMapper;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.ProfileService;
import com.lms.usermanagementservice.util.SecurityUtil;
import com.lms.usermanagementservice.validator.EmailValidator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private static final String PROFILE_CACHE_PREFIX =
            "PROFILE:";

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final SecurityUtil securityUtil;

    private final EmailValidator emailValidator;

    private final AuditLogService auditLogService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public ProfileResponse getCurrentProfile() {

        User currentUser =
                securityUtil.getCurrentUser();

        return mapToResponse(currentUser);
    }

    @Override
    public ProfileResponse getProfileByUserId(
            Long userId
    ) {

        User currentUser =
                securityUtil.getCurrentUser();

        validateProfileAccess(currentUser, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND
                        ));

        return mapToResponse(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(
            UpdateProfileRequest request
    ) {

        log.info(LogConstants.PROFILE_UPDATE_INITIATED);

        User currentUser =
                securityUtil.getCurrentUser();

        if (StringUtils.hasText(request.getFirstName())) {
            currentUser.setFirstName(
                    request.getFirstName()
            );
        }

        if (StringUtils.hasText(request.getLastName())) {
            currentUser.setLastName(
                    request.getLastName()
            );
        }

        if (StringUtils.hasText(request.getPhoneNumber())) {

            boolean phoneExists =
                    userRepository.existsByMobileNumber(
                            request.getPhoneNumber()
                    );

            if (phoneExists
                    && !request.getPhoneNumber()
                    .equals(currentUser.getPhoneNumber())) {

                throw new DuplicateResourceException(
                        MessageConstants.PHONE_ALREADY_EXISTS
                );
            }

            currentUser.setPhoneNumber(
                    request.getPhoneNumber()
            );
        }

        if (request.getGender() != null) {
            currentUser.setGender(
                    request.getGender()
            );
        }

        if (request.getDateOfBirth() != null) {
            currentUser.setDateOfBirth(
                    request.getDateOfBirth()
            );
        }

        User updatedUser =
                userRepository.save(currentUser);

        cacheProfile(updatedUser);

        auditLogService.createAuditLog(
                updatedUser.getId(),
                "PROFILE_UPDATED",
                "Profile updated successfully"
        );

        log.info(LogConstants.PROFILE_UPDATE_SUCCESS);

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public ProfileResponse uploadProfilePicture(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            throw new ValidationException(
                    MessageConstants.FILE_REQUIRED
            );
        }

        validateProfileImage(file);

        User currentUser =
                securityUtil.getCurrentUser();

        String fileName =
                UUID.randomUUID()
                        + "_"
                        + file.getOriginalFilename();

        String imageUrl =
                "/uploads/profile/" + fileName;

        currentUser.setProfileImage(imageUrl);

        User updatedUser =
                userRepository.save(currentUser);

        cacheProfile(updatedUser);

        auditLogService.createAuditLog(
                updatedUser.getId(),
                "PROFILE_PICTURE_UPLOADED",
                "Profile picture uploaded"
        );

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public void removeProfilePicture() {

        User currentUser =
                securityUtil.getCurrentUser();

        currentUser.setProfileImage(null);

        userRepository.save(currentUser);

        redisTemplate.delete(
                PROFILE_CACHE_PREFIX
                        + currentUser.getId()
        );

        auditLogService.createAuditLog(
                currentUser.getId(),
                "PROFILE_PICTURE_REMOVED",
                "Profile picture removed"
        );
    }

    @Override
    @Transactional
    public ProfileResponse updateEmail(
            Long userId,
            String email
    ) {

        validateAdminAccess();

        if (!emailValidator.isValid(email)) {
            throw new ValidationException(
                    MessageConstants.INVALID_EMAIL
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    MessageConstants.EMAIL_ALREADY_EXISTS
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND
                        ));

        user.setEmail(email);
        user.setEmailVerified(false);

        User updatedUser =
                userRepository.save(user);

        cacheProfile(updatedUser);

        auditLogService.createAuditLog(
                updatedUser.getId(),
                "EMAIL_UPDATED",
                "Email updated successfully"
        );

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public ProfileResponse updatePhoneNumber(
            Long userId,
            String phoneNumber
    ) {

        validateAdminAccess();

        if (userRepository.existsByMobileNumber(
                phoneNumber
        )) {

            throw new DuplicateResourceException(
                    MessageConstants.PHONE_ALREADY_EXISTS
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND
                        ));

        user.setPhoneNumber(phoneNumber);

        User updatedUser =
                userRepository.save(user);

        cacheProfile(updatedUser);

        auditLogService.createAuditLog(
                updatedUser.getId(),
                "PHONE_UPDATED",
                "Phone number updated"
        );

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public ProfileResponse activateProfile(
            Long userId
    ) {

        validateAdminAccess();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND
                        ));

        user.setStatus(UserStatus.ACTIVE);

        User updatedUser =
                userRepository.save(user);

        auditLogService.createAuditLog(
                updatedUser.getId(),
                "PROFILE_ACTIVATED",
                "Profile activated"
        );

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public ProfileResponse deactivateProfile(
            Long userId
    ) {

        validateAdminAccess();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND
                        ));

        user.setStatus(UserStatus.REJECTED);

        User updatedUser =
                userRepository.save(user);

        auditLogService.createAuditLog(
                updatedUser.getId(),
                "PROFILE_DEACTIVATED",
                "Profile deactivated"
        );

        return mapToResponse(updatedUser);
    }

    @Override
    public PageResponse<ProfileResponse> searchProfiles(
            String keyword,
            int page,
            int size
    ) {

        validateAdminAccess();

        Pageable pageable =
                PageRequest.of(page, size);

        Specification<User> specification =
                (root, query, criteriaBuilder) -> {

                    List<Predicate> predicates =
                            new ArrayList<>();

                    if (StringUtils.hasText(keyword)) {

                        predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("firstName")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("lastName")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("email")
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

        Page<User> userPage =
                userRepository.findAll(
                        specification,
                        pageable
                );

        List<ProfileResponse> responses =
                userPage.getContent()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return PageResponse.<ProfileResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .last(userPage.isLast())
                .build();
    }

    private void validateProfileAccess(
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

    private void validateProfileImage(
            MultipartFile file
    ) {

        String contentType =
                file.getContentType();

        if (contentType == null
                || (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png")
                && !contentType.equals("image/jpg"))) {

            throw new ValidationException(
                    MessageConstants.INVALID_FILE_FORMAT
            );
        }

        long maxSize =
                5 * 1024 * 1024;

        if (file.getSize() > maxSize) {

            throw new ValidationException(
                    MessageConstants.FILE_SIZE_EXCEEDED
            );
        }
    }

    private ProfileResponse mapToResponse(
            User user
    ) {

        return ProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .profileImage(user.getProfileImage())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private void cacheProfile(
            User user
    ) {

        redisTemplate.opsForValue().set(
                PROFILE_CACHE_PREFIX + user.getId(),
                user,
                Duration.ofHours(6)
        );
    }
}
