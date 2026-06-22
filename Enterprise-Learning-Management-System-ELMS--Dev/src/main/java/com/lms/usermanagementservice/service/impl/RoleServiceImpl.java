package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.request.AssignRoleRequest;
import com.lms.usermanagementservice.dto.request.CreateRoleRequest;
import com.lms.usermanagementservice.dto.request.UpdateRoleRequest;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.RoleResponse;
import com.lms.usermanagementservice.entity.Role;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.entity.UserRole;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.exception.DuplicateResourceException;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.mapper.RoleMapper;
import com.lms.usermanagementservice.repository.RoleRepository;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.repository.UserRoleRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.RoleService;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final String ROLE_CACHE_PREFIX =
            "ROLE:";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    private final RoleMapper roleMapper;

    private final SecurityUtil securityUtil;

    private final AuditLogService auditLogService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public RoleResponse createRole(
            CreateRoleRequest request
    ) {

        log.info(LogConstants.ROLE_CREATE_INITIATED);

        validateSuperAdminAccess();

        validateCreateRoleRequest(request);

        if (roleRepository.existsByName(
                request.getName()
        )) {

            throw new DuplicateResourceException(
                    MessageConstants.ROLE_ALREADY_EXISTS
            );
        }

        Role role = roleMapper.toEntity(request);

        role.setIsDeleted(false);
        role.setEnabled(true);

        Role savedRole =
                roleRepository.save(role);

        cacheRole(savedRole);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "ROLE_CREATED",
                "Role created successfully"
        );

        log.info(LogConstants.ROLE_CREATE_SUCCESS);

        return roleMapper.toResponse(savedRole);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(
            Long roleId,
            UpdateRoleRequest request
    ) {

        validateSuperAdminAccess();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.ROLE_NOT_FOUND
                        ));

        if (StringUtils.hasText(request.getName())) {

            boolean roleExists =
                    roleRepository.existsByName(
                            request.getName()
                    );

            if (roleExists
                    && !role.getName()
                    .equals(request.getName())) {

                throw new DuplicateResourceException(
                        MessageConstants.ROLE_ALREADY_EXISTS
                );
            }

            role.setName(request.getName());
        }

        if (StringUtils.hasText(request.getDescription())) {
            role.setDescription(
                    request.getDescription()
            );
        }

        Role updatedRole =
                roleRepository.save(role);

        cacheRole(updatedRole);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "ROLE_UPDATED",
                "Role updated successfully"
        );

        return roleMapper.toResponse(updatedRole);
    }

    @Override
    public RoleResponse getRoleById(
            Long roleId
    ) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.ROLE_NOT_FOUND
                        ));

        return roleMapper.toResponse(role);
    }

    @Override
    public RoleResponse getRoleByName(
            String roleName
    ) {

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.ROLE_NOT_FOUND
                        ));

        return roleMapper.toResponse(role);
    }

    @Override
    public PageResponse<RoleResponse> getAllRoles(
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

        Page<Role> rolePage =
                roleRepository.findAll(pageable);

        List<RoleResponse> responses =
                rolePage.getContent()
                        .stream()
                        .map(roleMapper::toResponse)
                        .toList();

        return PageResponse.<RoleResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(rolePage.getTotalPages())
                .totalElements(rolePage.getTotalElements())
                .last(rolePage.isLast())
                .build();
    }

    @Override
    public PageResponse<RoleResponse> searchRoles(
            String keyword,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Specification<Role> specification =
                (root, query, criteriaBuilder) -> {

                    List<Predicate> predicates =
                            new ArrayList<>();

                    if (StringUtils.hasText(keyword)) {

                        predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("roleName")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("description")
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

        Page<Role> rolePage =
                roleRepository.findAll(
                        specification,
                        pageable
                );

        List<RoleResponse> responses =
                rolePage.getContent()
                        .stream()
                        .map(roleMapper::toResponse)
                        .toList();

        return PageResponse.<RoleResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(rolePage.getTotalPages())
                .totalElements(rolePage.getTotalElements())
                .last(rolePage.isLast())
                .build();
    }

    @Override
    @Transactional
    public RoleResponse assignRoleToUser(
            AssignRoleRequest request
    ) {

        validateSuperAdminAccess();

        User user = userRepository.findById(
                        request.getUserId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND
                        ));

        Role role = roleRepository.findById(
                        request.getRoleId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.ROLE_NOT_FOUND
                        ));

        boolean alreadyAssigned =
                userRoleRepository.existsByUserIdAndRoleId(
                        user.getId(),
                        role.getId()
                );

        if (alreadyAssigned) {
            throw new DuplicateResourceException(
                    MessageConstants.ROLE_ALREADY_ASSIGNED
            );
        }

        UserRole userRole = new UserRole();

        userRole.setUser(user);
        userRole.setRole(role);

        userRoleRepository.save(userRole);

        auditLogService.createAuditLog(
                user.getId(),
                "ROLE_ASSIGNED",
                "Role assigned successfully"
        );

        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse removeRoleFromUser(
            AssignRoleRequest request
    ) {

        validateSuperAdminAccess();

        UserRole userRole =
                userRoleRepository
                        .findByUserIdAndRoleId(
                                request.getUserId(),
                                request.getRoleId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.USER_ROLE_NOT_FOUND
                                ));

        if (userRole.getRole().getName()
                .equals(UserRoleType.SUPER_ADMIN.name())) {

            throw new ValidationException(
                    MessageConstants.SUPER_ADMIN_ROLE_CANNOT_BE_REMOVED
            );
        }

        userRoleRepository.delete(userRole);

        auditLogService.createAuditLog(
                request.getUserId(),
                "ROLE_REMOVED",
                "Role removed successfully"
        );

        return roleMapper.toResponse(
                userRole.getRole()
        );
    }

    @Override
    public List<RoleResponse> getUserRoles(
            Long userId
    ) {

        List<UserRole> userRoles =
                userRoleRepository.findAllByUserId(
                        userId
                );

        return userRoles.stream()
                .map(UserRole::getRole)
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    public boolean  hasRole(
            Long userId,
            String roleName
    ) {

        return userRoleRepository
                .existsByUserIdAndRoleName(
                        userId,
                        roleName
                );
    }

    @Override
    @Transactional
    public RoleResponse activateRole(
            Long roleId
    ) {

        validateSuperAdminAccess();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.ROLE_NOT_FOUND
                        ));

        role.setEnabled(true);

        Role updatedRole =
                roleRepository.save(role);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "ROLE_ACTIVATED",
                "Role activated"
        );

        return roleMapper.toResponse(updatedRole);
    }

    @Override
    @Transactional
    public RoleResponse deactivateRole(
            Long roleId
    ) {

        validateSuperAdminAccess();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.ROLE_NOT_FOUND
                        ));

        if (role.getName()
                .equals(UserRoleType.SUPER_ADMIN.name())) {

            throw new ValidationException(
                    MessageConstants.SUPER_ADMIN_ROLE_CANNOT_BE_DISABLED
            );
        }

        role.setEnabled(false);

        Role updatedRole =
                roleRepository.save(role);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "ROLE_DEACTIVATED",
                "Role deactivated"
        );

        return roleMapper.toResponse(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {

        validateSuperAdminAccess();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.ROLE_NOT_FOUND
                        ));

        if (role.getName()
                .equals(UserRoleType.SUPER_ADMIN.name())) {

            throw new ValidationException(
                    MessageConstants.SUPER_ADMIN_ROLE_CANNOT_BE_DELETED
            );
        }

        roleRepository.delete(role);

        redisTemplate.delete(
                ROLE_CACHE_PREFIX + roleId
        );

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "ROLE_DELETED",
                "Role deleted permanently"
        );
    }

    @Override
    @Transactional
    public void softDeleteRole(Long roleId) {

        validateSuperAdminAccess();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.ROLE_NOT_FOUND
                        ));

        if (role.getName()
                .equals(UserRoleType.SUPER_ADMIN.name())) {

            throw new ValidationException(
                    MessageConstants.SUPER_ADMIN_ROLE_CANNOT_BE_DELETED
            );
        }

        role.setIsDeleted(true);
        role.setEnabled(false);

        roleRepository.save(role);

        redisTemplate.delete(
                ROLE_CACHE_PREFIX + roleId
        );

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "ROLE_SOFT_DELETED",
                "Role soft deleted"
        );
    }

    private void validateCreateRoleRequest(
            CreateRoleRequest request
    ) {

        if (!StringUtils.hasText(request.getName())) {
            throw new ValidationException(
                    MessageConstants.ROLE_NAME_REQUIRED
            );
        }

        if (!StringUtils.hasText(
                request.getDescription()
        )) {

            throw new ValidationException(
                    MessageConstants.ROLE_DESCRIPTION_REQUIRED
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
                                UserRoleType.SUPER_ADMIN.name()
                                        .equals(role.getRole().getName())
                                ||
                                ("ROLE_" + UserRoleType.SUPER_ADMIN.name())
                                        .equals(role.getRole().getName())
                        );

        if (!isSuperAdmin) {

            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }
    }

    private void cacheRole(Role role) {

        redisTemplate.opsForValue().set(
                ROLE_CACHE_PREFIX + role.getId(),
                role,
                Duration.ofHours(12)
        );
    }
}