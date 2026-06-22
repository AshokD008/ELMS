package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.request.CreateCollegeAdminRequest;
import com.lms.usermanagementservice.dto.request.UpdateCollegeAdminRequest;
import com.lms.usermanagementservice.dto.response.CollegeAdminResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.TokenResponse;
import com.lms.usermanagementservice.dto.response.ProfileResponse;
import com.lms.usermanagementservice.enums.AccountType;
import com.lms.usermanagementservice.enums.ApprovalStage;
import com.lms.usermanagementservice.entity.College;
import com.lms.usermanagementservice.entity.CollegeAdminProfile;
import com.lms.usermanagementservice.entity.Role;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.entity.UserRole;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.enums.UserStatus;
import com.lms.usermanagementservice.exception.DuplicateResourceException;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.mapper.UserMapper;
import com.lms.usermanagementservice.repository.CollegeAdminProfileRepository;
import com.lms.usermanagementservice.repository.CollegeRepository;
import com.lms.usermanagementservice.repository.RoleRepository;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.repository.UserRoleRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.CollegeAdminService;
import com.lms.usermanagementservice.util.PasswordUtil;
import com.lms.usermanagementservice.util.SecurityUtil;
import com.lms.usermanagementservice.validator.EmailValidator;
import com.lms.usermanagementservice.validator.PasswordValidator;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeAdminServiceImpl
        implements CollegeAdminService {

    private static final String COLLEGE_ADMIN_CACHE_PREFIX =
            "COLLEGE_ADMIN:";

    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final CollegeAdminProfileRepository
            collegeAdminProfileRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    private final UserMapper userMapper;

    private final PasswordUtil passwordUtil;
    private final SecurityUtil securityUtil;

    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;

    private final AuditLogService auditLogService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProfileResponse> getFacultyAndStudentApprovals(Long adminId, int page, int size) {
        requireAdminOwnership(adminId);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAllByStatusAndEmailVerifiedTrueAndCollegeAdmin_Id(
                UserStatus.PENDING_VERIFICATION, adminId, pageable);
        List<ProfileResponse> content = users.stream()
                .filter(user -> user.getAccountType() == AccountType.FACULTY
                        || user.getAccountType() == AccountType.STUDENT)
                .map(userMapper::toProfileResponse)
                .toList();
        return PageResponse.<ProfileResponse>builder().content(content).page(users.getNumber())
                .size(users.getSize()).totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements()).last(users.isLast()).build();
    }

    @Override
    @Transactional
    public TokenResponse approveCollegeUser(Long adminId, Long userId) {
        requireAdminOwnership(adminId);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));
        if (target.getStatus() == UserStatus.ACTIVE) {
            throw new ValidationException("User is already ACTIVE");
        }
        if (!Boolean.TRUE.equals(target.getEmailVerified())) {
            throw new ValidationException(MessageConstants.EMAIL_NOT_VERIFIED);
        }
        if (target.getStatus() != UserStatus.PENDING_VERIFICATION
                || !adminId.equals(target.getCollegeAdminId())
                || (target.getAccountType() != AccountType.FACULTY
                && target.getAccountType() != AccountType.STUDENT)) {
            throw new ForbiddenException(MessageConstants.ACCESS_DENIED);
        }
        target.setStatus(UserStatus.ACTIVE);
        target.setApprovalStage(ApprovalStage.COLLEGE_ADMIN_APPROVED);
        target.setApprovedByCollegeAdmin(adminId);
        target.setCollegeAdminApprovedAt(LocalDateTime.now());
        userRepository.save(target);
        return TokenResponse.builder().success(true).message("College user approved successfully").build();
    }

    private void requireAdminOwnership(Long adminId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(adminId)) {
            throw new ForbiddenException(MessageConstants.ACCESS_DENIED);
        }
    }

    @Override
    @Transactional
    public CollegeAdminResponse createCollegeAdmin(
            CreateCollegeAdminRequest request
    ) {

        log.info(LogConstants.COLLEGE_ADMIN_CREATE_INITIATED);

        validateCreateRequest(request);

        validateSuperAdminAccess();

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    MessageConstants.EMAIL_ALREADY_EXISTS
            );
        }

        College college = collegeRepository
                .findById(request.getCollegeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.COLLEGE_NOT_FOUND
                        ));

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(
                passwordUtil.encode(request.getPassword())
        );
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        user.setIsDeleted(false);
        user.setEmailVerified(true);

        User savedUser = userRepository.save(user);

        Role role = roleRepository
                .findByRoleName(UserRoleType.COLLEGE_ADMIN.name())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.ROLE_NOT_FOUND
                        ));

        UserRole userRole = new UserRole();

        userRole.setUser(savedUser);
        userRole.setRole(role);

        userRoleRepository.save(userRole);

        CollegeAdminProfile profile =
                new CollegeAdminProfile();

        profile.setUser(savedUser);
        profile.setCollege(college);
        profile.setDesignation(request.getDesignation());
        profile.setEmployeeId(request.getEmployeeId());
        profile.setJoiningDate(LocalDateTime.now());

        CollegeAdminProfile savedProfile =
                collegeAdminProfileRepository.save(profile);

        cacheCollegeAdmin(savedProfile);

        auditLogService.createAuditLog(
                savedUser.getId(),
                "COLLEGE_ADMIN_CREATED",
                "College admin created successfully"
        );

        log.info(LogConstants.COLLEGE_ADMIN_CREATE_SUCCESS);

        return mapToResponse(savedProfile);
    }

    @Override
    @Transactional
    public CollegeAdminResponse updateCollegeAdmin(
            Long adminId,
            UpdateCollegeAdminRequest request
    ) {

        CollegeAdminProfile profile =
                collegeAdminProfileRepository.findById(adminId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_ADMIN_NOT_FOUND
                                ));

        validateAdminOwnership(profile);

        User user = profile.getUser();

        if (StringUtils.hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }

        if (StringUtils.hasText(request.getLastName())) {
            user.setLastName(request.getLastName());
        }

        if (StringUtils.hasText(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (StringUtils.hasText(request.getDesignation())) {
            profile.setDesignation(request.getDesignation());
        }

        if (StringUtils.hasText(request.getEmployeeId())) {
            profile.setEmployeeId(request.getEmployeeId());
        }

        userRepository.save(user);

        CollegeAdminProfile updatedProfile =
                collegeAdminProfileRepository.save(profile);

        cacheCollegeAdmin(updatedProfile);

        auditLogService.createAuditLog(
                user.getId(),
                "COLLEGE_ADMIN_UPDATED",
                "College admin updated successfully"
        );

        return mapToResponse(updatedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeAdminResponse getCollegeAdminById(
            Long adminId
    ) {

        CollegeAdminProfile profile =
                collegeAdminProfileRepository.findWithUserAndCollegeById(adminId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_ADMIN_NOT_FOUND
                                ));

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CollegeAdminResponse>
    getAllCollegeAdmins(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {

        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Page<CollegeAdminProfile> adminPage =
                collegeAdminProfileRepository.findAll(
                        pageable
                );

        List<CollegeAdminResponse> responses =
                adminPage.getContent()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return PageResponse.<CollegeAdminResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(adminPage.getTotalPages())
                .totalElements(adminPage.getTotalElements())
                .last(adminPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CollegeAdminResponse>
    searchCollegeAdmins(
            String keyword,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Specification<CollegeAdminProfile> specification =
                (root, query, criteriaBuilder) -> {

                    List<Predicate> predicates =
                            new ArrayList<>();

                    if (StringUtils.hasText(keyword)) {

                        predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("designation")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("employeeId")
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

        Page<CollegeAdminProfile> adminPage =
                collegeAdminProfileRepository.findAll(
                        specification,
                        pageable
                );

        List<CollegeAdminResponse> responses =
                adminPage.getContent()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return PageResponse.<CollegeAdminResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(adminPage.getTotalPages())
                .totalElements(adminPage.getTotalElements())
                .last(adminPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeAdminResponse>
    getCollegeAdminsByCollege(Long collegeId) {

        List<CollegeAdminProfile> profiles =
                collegeAdminProfileRepository
                        .findAllByCollegeId(collegeId);

        return profiles.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public CollegeAdminResponse activateCollegeAdmin(
            Long adminId
    ) {

        CollegeAdminProfile profile =
                collegeAdminProfileRepository.findById(adminId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_ADMIN_NOT_FOUND
                                ));

        profile.getUser().setStatus(UserStatus.ACTIVE);

        collegeAdminProfileRepository.save(profile);

        auditLogService.createAuditLog(
                profile.getUser().getId(),
                "COLLEGE_ADMIN_ACTIVATED",
                "College admin activated"
        );

        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public CollegeAdminResponse deactivateCollegeAdmin(
            Long adminId
    ) {

        CollegeAdminProfile profile =
                collegeAdminProfileRepository.findById(adminId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_ADMIN_NOT_FOUND
                                ));

        profile.getUser().setStatus(UserStatus.REJECTED);

        collegeAdminProfileRepository.save(profile);

        auditLogService.createAuditLog(
                profile.getUser().getId(),
                "COLLEGE_ADMIN_DEACTIVATED",
                "College admin deactivated"
        );

        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public void deleteCollegeAdmin(Long adminId) {

        validateSuperAdminAccess();

        CollegeAdminProfile profile =
                collegeAdminProfileRepository.findById(adminId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_ADMIN_NOT_FOUND
                                ));

        collegeAdminProfileRepository.delete(profile);

        userRepository.delete(profile.getUser());

        redisTemplate.delete(
                COLLEGE_ADMIN_CACHE_PREFIX + adminId
        );

        auditLogService.createAuditLog(
                profile.getUser().getId(),
                "COLLEGE_ADMIN_DELETED",
                "College admin deleted permanently"
        );
    }

    @Override
    @Transactional
    public void softDeleteCollegeAdmin(Long adminId) {

        validateSuperAdminAccess();

        CollegeAdminProfile profile =
                collegeAdminProfileRepository.findById(adminId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_ADMIN_NOT_FOUND
                                ));

        User user = profile.getUser();

        user.setIsDeleted(true);
        user.setStatus(UserStatus.REJECTED);

        userRepository.save(user);

        redisTemplate.delete(
                COLLEGE_ADMIN_CACHE_PREFIX + adminId
        );

        auditLogService.createAuditLog(
                user.getId(),
                "COLLEGE_ADMIN_SOFT_DELETED",
                "College admin soft deleted"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {

        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeAdminResponse
    getCurrentCollegeAdminProfile() {

        User currentUser =
                securityUtil.getCurrentUser();

        CollegeAdminProfile profile =
                collegeAdminProfileRepository
                        .findByUserId(currentUser.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_ADMIN_NOT_FOUND
                                ));

        return mapToResponse(profile);
    }

    private void validateCreateRequest(
            CreateCollegeAdminRequest request
    ) {

        if (!StringUtils.hasText(request.getFirstName())) {
            throw new ValidationException(
                    MessageConstants.FIRST_NAME_REQUIRED
            );
        }

        if (!StringUtils.hasText(request.getEmail())) {
            throw new ValidationException(
                    MessageConstants.EMAIL_REQUIRED
            );
        }

        if (!emailValidator.isValid(request.getEmail())) {
            throw new ValidationException(
                    MessageConstants.INVALID_EMAIL
            );
        }

        passwordValidator.validate(
                request.getPassword()
        );

        if (request.getCollegeId() == null) {
            throw new ValidationException(
                    MessageConstants.COLLEGE_ID_REQUIRED
            );
        }
    }

    private void validateSuperAdminAccess() {

        User currentUser =
                securityUtil.getCurrentUser();

        boolean isSuperAdmin =
                currentUser.getUserRoles()
                        .stream()
                        .anyMatch(role ->
                                isRole(role.getRole().getName(), UserRoleType.SUPER_ADMIN)
                        );

        if (!isSuperAdmin) {
            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }
    }

    private void validateAdminOwnership(
            CollegeAdminProfile profile
    ) {

        User currentUser =
                securityUtil.getCurrentUser();

        boolean isSuperAdmin =
                currentUser.getUserRoles()
                        .stream()
                        .anyMatch(role ->
                                isRole(role.getRole().getName(), UserRoleType.SUPER_ADMIN)
                        );

        if (!isSuperAdmin
                && !profile.getUser()
                .getId()
                .equals(currentUser.getId())) {

            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }
    }

    private CollegeAdminResponse mapToResponse(
            CollegeAdminProfile profile
    ) {

        return CollegeAdminResponse.builder()
                .id(profile.getId())
                .user(
                        userMapper.toProfileResponse(
                                profile.getUser()
                        )
                )
                .collegeId(
                        profile.getCollege().getId()
                )
                .collegeName(
                        profile.getCollege().getName()
                )
                .designation(profile.getDesignation())
                .employeeId(profile.getEmployeeId())
                .joiningDate(profile.getJoiningDate())
                .build();
    }

    private void cacheCollegeAdmin(
            CollegeAdminProfile profile
    ) {

        redisTemplate.opsForValue().set(
                COLLEGE_ADMIN_CACHE_PREFIX
                        + profile.getId(),
                mapToResponse(profile)
        );
    }

    private boolean isRole(String actualRole, UserRoleType expectedRole) {

        return expectedRole.name().equals(actualRole)
                || ("ROLE_" + expectedRole.name()).equals(actualRole);
    }

//	@Override
//	public PageResponse<CollegeAdminResponse> getPendingCollegeAdmins(int page, int size) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public TokenResponse approveUser(Long userId) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public TokenResponse rejectUser(Long userId, String reason) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
