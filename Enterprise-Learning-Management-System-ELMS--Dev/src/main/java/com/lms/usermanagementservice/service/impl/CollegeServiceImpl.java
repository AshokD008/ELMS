package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.request.CreateCollegeRequest;
import com.lms.usermanagementservice.dto.request.UpdateCollegeRequest;
import com.lms.usermanagementservice.dto.response.CollegeResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.entity.College;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.CollegeStatus;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.exception.DuplicateResourceException;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.mapper.CollegeMapper;
import com.lms.usermanagementservice.repository.CollegeRepository;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.repository.CollegeAdminProfileRepository;
import com.lms.usermanagementservice.repository.StudentProfileRepository;
import com.lms.usermanagementservice.repository.FacultyProfileRepository;
import com.lms.usermanagementservice.service.IdGenerationService;
import com.lms.usermanagementservice.enums.AccountType;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.CollegeService;
import com.lms.usermanagementservice.util.SecurityUtil;
import com.lms.usermanagementservice.validator.CollegeValidator;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeServiceImpl implements CollegeService {

    private static final String COLLEGE_CACHE_PREFIX =
            "COLLEGE:";

    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;
    private final CollegeAdminProfileRepository collegeAdminProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final IdGenerationService idGenerationService;

    private final CollegeMapper collegeMapper;

    private final CollegeValidator collegeValidator;

    private final SecurityUtil securityUtil;

    private final AuditLogService auditLogService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public CollegeResponse createCollege(
            CreateCollegeRequest request
    ) {

        log.info(LogConstants.COLLEGE_CREATE_INITIATED);

        Long adminId = SecurityUtil.getCurrentUserId();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));
        if (admin.getAccountType() != AccountType.COLLEGE_ADMIN || admin.getStatus() != com.lms.usermanagementservice.enums.UserStatus.ACTIVE) {
            throw new ForbiddenException(MessageConstants.ACCESS_DENIED);
        }

        validateCreateCollegeRequest(request);

        String generatedCode = idGenerationService.nextCollegeCode(request.getCollegeName());

        if (collegeRepository.existsByEmail(
                request.getEmail()
        )) {

            throw new DuplicateResourceException(
                    MessageConstants.EMAIL_ALREADY_EXISTS
            );
        }

        College college =
                collegeMapper.toEntity(request);

        college.setCollegeCode(generatedCode);

        college.setStatus(CollegeStatus.ACTIVE);
        college.setIsDeleted(false);

        College savedCollege =
                collegeRepository.save(college);

        admin.setCollege(savedCollege);
        userRepository.save(admin);
        collegeAdminProfileRepository.findByUserId(adminId).ifPresent(profile -> {
            profile.setCollege(savedCollege);
            collegeAdminProfileRepository.save(profile);
        });

        List<User> scopedUsers = userRepository.findAllByCollegeAdmin_Id(adminId);
        scopedUsers.forEach(user -> user.setCollege(savedCollege));
        userRepository.saveAll(scopedUsers);
        scopedUsers.forEach(user -> {
            studentProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                profile.setCollege(savedCollege);
                studentProfileRepository.save(profile);
            });
            facultyProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                profile.setCollege(savedCollege);
                facultyProfileRepository.save(profile);
            });
        });

        cacheCollege(savedCollege);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "COLLEGE_CREATED",
                "College created successfully"
        );

        log.info(LogConstants.COLLEGE_CREATE_SUCCESS);

        return collegeMapper.toResponse(savedCollege);
    }

    @Override
    @Transactional
    public CollegeResponse updateCollege(
            Long collegeId,
            UpdateCollegeRequest request
    ) {

        validateSuperAdminAccess();

        College college =
                collegeRepository.findById(collegeId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_NOT_FOUND
                                ));

        if (StringUtils.hasText(request.getCollegeName())) {
            college.setName(request.getCollegeName());
        }

        if (StringUtils.hasText(request.getEmail())) {

            boolean emailExists =
                    collegeRepository.existsByEmail(
                            request.getEmail()
                    );

            if (emailExists
                    && !college.getEmail()
                    .equals(request.getEmail())) {

                throw new DuplicateResourceException(
                        MessageConstants.EMAIL_ALREADY_EXISTS
                );
            }

            college.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getPhoneNumber())) {
            college.setPhoneNumber(
                    request.getPhoneNumber()
            );
        }

        if (StringUtils.hasText(request.getAddress())) {
            college.setAddress(
                    request.getAddress()
            );
        }

        if (StringUtils.hasText(request.getCity())) {
            college.setCity(request.getCity());
        }

        if (StringUtils.hasText(request.getState())) {
            college.setState(request.getState());
        }

        if (StringUtils.hasText(request.getCountry())) {
            college.setCountry(
                    request.getCountry()
            );
        }

        if (StringUtils.hasText(request.getPostalCode())) {
            college.setPostalCode(
                    request.getPostalCode()
            );
        }

        College updatedCollege =
                collegeRepository.save(college);

        cacheCollege(updatedCollege);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "COLLEGE_UPDATED",
                "College updated successfully"
        );

        return collegeMapper.toResponse(updatedCollege);
    }

    @Override
    public CollegeResponse getCollegeById(
            Long collegeId
    ) {

        College college =
                collegeRepository.findById(collegeId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_NOT_FOUND
                                ));

        return collegeMapper.toResponse(college);
    }

    @Override
    public CollegeResponse getCollegeByCode(
            String code
    ) {

        College college =
                collegeRepository.findByCollegeCode(code)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_NOT_FOUND
                                ));

        return collegeMapper.toResponse(college);
    }

    @Override
    public PageResponse<CollegeResponse> getAllColleges(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {

        Sort sort =
                sortDirection.equalsIgnoreCase("DESC")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Page<College> collegePage =
                collegeRepository.findAll(pageable);

        List<CollegeResponse> responses =
                collegePage.getContent()
                        .stream()
                        .map(collegeMapper::toResponse)
                        .toList();

        return PageResponse.<CollegeResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(collegePage.getTotalPages())
                .totalElements(collegePage.getTotalElements())
                .last(collegePage.isLast())
                .build();
    }

    @Override
    public PageResponse<CollegeResponse> searchColleges(
            String keyword,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Specification<College> specification =
                (root, query, criteriaBuilder) -> {

                    List<Predicate> predicates =
                            new ArrayList<>();

                    if (StringUtils.hasText(keyword)) {

                        predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("collegeName")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("collegeCode")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("city")
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

        Page<College> collegePage =
                collegeRepository.findAll(
                        specification,
                        pageable
                );

        List<CollegeResponse> responses =
                collegePage.getContent()
                        .stream()
                        .map(collegeMapper::toResponse)
                        .toList();

        return PageResponse.<CollegeResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(collegePage.getTotalPages())
                .totalElements(collegePage.getTotalElements())
                .last(collegePage.isLast())
                .build();
    }

    @Override
    public List<CollegeResponse> getActiveColleges() {

        List<College> colleges =
                collegeRepository.findAllByStatus(
                        CollegeStatus.ACTIVE
                );

        return colleges.stream()
                .map(collegeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CollegeResponse activateCollege(
            Long collegeId
    ) {

        validateSuperAdminAccess();

        College college =
                collegeRepository.findById(collegeId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_NOT_FOUND
                                ));

        college.setStatus(CollegeStatus.ACTIVE);

        College updatedCollege =
                collegeRepository.save(college);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "COLLEGE_ACTIVATED",
                "College activated"
        );

        return collegeMapper.toResponse(updatedCollege);
    }

    @Override
    @Transactional
    public CollegeResponse deactivateCollege(
            Long collegeId
    ) {

        validateSuperAdminAccess();

        College college =
                collegeRepository.findById(collegeId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_NOT_FOUND
                                ));

        college.setStatus(CollegeStatus.INACTIVE);

        College updatedCollege =
                collegeRepository.save(college);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "COLLEGE_DEACTIVATED",
                "College deactivated"
        );

        return collegeMapper.toResponse(updatedCollege);
    }

    @Override
    public boolean existsByCollegeCode(
            String collegeCode
    ) {
        return collegeRepository.existsByCollegeCode(
                collegeCode
        );
    }

    @Override
    public boolean existsByEmail(
            String email
    ) {
        return collegeRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void deleteCollege(Long collegeId) {

        validateSuperAdminAccess();

        College college =
                collegeRepository.findById(collegeId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_NOT_FOUND
                                ));

        collegeRepository.delete(college);

        redisTemplate.delete(
                COLLEGE_CACHE_PREFIX + collegeId
        );

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "COLLEGE_DELETED",
                "College deleted permanently"
        );
    }

    @Override
    @Transactional
    public void softDeleteCollege(
            Long collegeId
    ) {

        validateSuperAdminAccess();

        College college =
                collegeRepository.findById(collegeId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.COLLEGE_NOT_FOUND
                                ));

        college.setIsDeleted(true);
        college.setStatus(CollegeStatus.INACTIVE);

        collegeRepository.save(college);

        redisTemplate.delete(
                COLLEGE_CACHE_PREFIX + collegeId
        );

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "COLLEGE_SOFT_DELETED",
                "College soft deleted"
        );
    }

    private void validateCreateCollegeRequest(
            CreateCollegeRequest request
    ) {

        if (!StringUtils.hasText(
                request.getCollegeName()
        )) {

            throw new ValidationException(
                    MessageConstants.COLLEGE_NAME_REQUIRED
            );
        }

        if (!StringUtils.hasText(
                request.getCollegeCode()
        )) {

            throw new ValidationException(
                    MessageConstants.COLLEGE_CODE_REQUIRED
            );
        }

        collegeValidator.validate(request);
    }

    private void validateSuperAdminAccess() {

        User currentUser = securityUtil.getCurrentUser();

        currentUser.getUserRoles().forEach(role ->
                System.out.println("ROLE = " +
                        role.getRole().getRoleName()));

        boolean isSuperAdmin =
                currentUser.getUserRoles()
                        .stream()
                        .anyMatch(role ->
                                role.getRole()
                                        .getRoleName()
                                        .equalsIgnoreCase("ROLE_SUPER_ADMIN")
                        );

        if (!isSuperAdmin) {
            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }
    }
    private void cacheCollege(
            College college
    ) {

        redisTemplate.opsForValue().set(
                COLLEGE_CACHE_PREFIX
                        + college.getId(),
                collegeMapper.toResponse(college),
                Duration.ofHours(12)
        );
    }
}
