package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.request.AssignPermissionRequest;
import com.lms.usermanagementservice.dto.request.CreatePermissionRequest;
import com.lms.usermanagementservice.dto.request.UpdatePermissionRequest;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.PermissionResponse;
import com.lms.usermanagementservice.entity.Permission;
import com.lms.usermanagementservice.entity.Role;
import com.lms.usermanagementservice.entity.RolePermission;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.exception.DuplicateResourceException;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.mapper.PermissionMapper;
import com.lms.usermanagementservice.repository.PermissionRepository;
import com.lms.usermanagementservice.repository.RolePermissionRepository;
import com.lms.usermanagementservice.repository.RoleRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.PermissionService;
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
public class PermissionServiceImpl
        implements PermissionService {

    private static final String PERMISSION_CACHE_PREFIX =
            "PERMISSION:";

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository
            rolePermissionRepository;

    private final PermissionMapper permissionMapper;

    private final SecurityUtil securityUtil;

    private final AuditLogService auditLogService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public PermissionResponse createPermission(
            CreatePermissionRequest request
    ) {

        log.info(LogConstants.PERMISSION_CREATE_INITIATED);

        validateSuperAdminAccess();

        validateCreatePermissionRequest(request);

        if (permissionRepository.existsByName(
                request.getName()
        )) {

            throw new DuplicateResourceException(
                    MessageConstants.PERMISSION_ALREADY_EXISTS
            );
        }

        Permission permission =
                permissionMapper.toEntity(request);

        permission.setIsDeleted(false);
        permission.setEnabled(true);

        Permission savedPermission =
                permissionRepository.save(permission);

        cachePermission(savedPermission);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "PERMISSION_CREATED",
                "Permission created successfully"
        );

        log.info(LogConstants.PERMISSION_CREATE_SUCCESS);

        return permissionMapper.toResponse(
                savedPermission
        );
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(
            Long permissionId,
            UpdatePermissionRequest request
    ) {

        validateSuperAdminAccess();

        Permission permission =
                permissionRepository.findById(permissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.PERMISSION_NOT_FOUND
                                ));

        if (StringUtils.hasText(request.getName())) {

            boolean exists =
                    permissionRepository.existsByName(
                            request.getName()
                    );

            if (exists
                    && !permission.getName()
                    .equals(request.getName())) {

                throw new DuplicateResourceException(
                        MessageConstants.PERMISSION_ALREADY_EXISTS
                );
            }

            permission.setName(request.getName());
        }

        if (StringUtils.hasText(
                request.getDescription()
        )) {

            permission.setDescription(
                    request.getDescription()
            );
        }

        Permission updatedPermission =
                permissionRepository.save(permission);

        cachePermission(updatedPermission);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "PERMISSION_UPDATED",
                "Permission updated successfully"
        );

        return permissionMapper.toResponse(
                updatedPermission
        );
    }

    @Override
    public PermissionResponse getPermissionById(
            Long permissionId
    ) {

        Permission permission =
                permissionRepository.findById(permissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.PERMISSION_NOT_FOUND
                                ));

        return permissionMapper.toResponse(permission);
    }

    @Override
    public PermissionResponse getPermissionByName(
            String permissionName
    ) {

        Permission permission =
                permissionRepository.findByPermissionName(
                                permissionName
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.PERMISSION_NOT_FOUND
                                ));

        return permissionMapper.toResponse(permission);
    }

    @Override
    public PageResponse<PermissionResponse>
    getAllPermissions(
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

        Page<Permission> permissionPage =
                permissionRepository.findAll(pageable);

        List<PermissionResponse> responses =
                permissionPage.getContent()
                        .stream()
                        .map(permissionMapper::toResponse)
                        .toList();

        return PageResponse.<PermissionResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(permissionPage.getTotalPages())
                .totalElements(permissionPage.getTotalElements())
                .last(permissionPage.isLast())
                .build();
    }

    @Override
    public PageResponse<PermissionResponse>
    searchPermissions(
            String keyword,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Specification<Permission> specification =
                (root, query, criteriaBuilder) -> {

                    List<Predicate> predicates =
                            new ArrayList<>();

                    if (StringUtils.hasText(keyword)) {

                        predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("permissionName")
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

        Page<Permission> permissionPage =
                permissionRepository.findAll(
                        specification,
                        pageable
                );

        List<PermissionResponse> responses =
                permissionPage.getContent()
                        .stream()
                        .map(permissionMapper::toResponse)
                        .toList();

        return PageResponse.<PermissionResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(permissionPage.getTotalPages())
                .totalElements(permissionPage.getTotalElements())
                .last(permissionPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public PermissionResponse assignPermissionToRole(
            AssignPermissionRequest request
    ) {

        validateSuperAdminAccess();

        Role role = roleRepository.findById(
                        request.getRoleId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.ROLE_NOT_FOUND
                        ));

        Permission permission =
                permissionRepository.findById(
                                request.getPermissionId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.PERMISSION_NOT_FOUND
                                ));

        boolean alreadyAssigned =
                rolePermissionRepository
                        .existsByRoleIdAndPermissionId(
                                role.getId(),
                                permission.getId()
                        );

        if (alreadyAssigned) {

            throw new DuplicateResourceException(
                    MessageConstants.PERMISSION_ALREADY_ASSIGNED
            );
        }

        RolePermission rolePermission =
                new RolePermission();

        rolePermission.setRole(role);
        rolePermission.setPermission(permission);

        rolePermissionRepository.save(rolePermission);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "PERMISSION_ASSIGNED",
                "Permission assigned to role"
        );

        return permissionMapper.toResponse(
                permission
        );
    }

    @Override
    @Transactional
    public PermissionResponse removePermissionFromRole(
            AssignPermissionRequest request
    ) {

        validateSuperAdminAccess();

        RolePermission rolePermission =
                rolePermissionRepository
                        .findByRoleIdAndPermissionId(
                                request.getRoleId(),
                                request.getPermissionId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.ROLE_PERMISSION_NOT_FOUND
                                ));

        rolePermissionRepository.delete(rolePermission);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "PERMISSION_REMOVED",
                "Permission removed from role"
        );

        return permissionMapper.toResponse(
                rolePermission.getPermission()
        );
    }

    @Override
    public List<PermissionResponse> getRolePermissions(
            Long roleId
    ) {

        List<RolePermission> permissions =
                rolePermissionRepository
                        .findAllByRoleId(roleId);

        return permissions.stream()
                .map(RolePermission::getPermission)
                .map(permissionMapper::toResponse)
                .toList();
    }

    @Override
    public Boolean hasPermission(
            Long userId,
            String permissionName
    ) {

        return rolePermissionRepository
                .existsByUserIdAndPermissionName(
                        userId,
                        permissionName
                );
    }

    @Override
    @Transactional
    public PermissionResponse activatePermission(
            Long permissionId
    ) {

        validateSuperAdminAccess();

        Permission permission =
                permissionRepository.findById(permissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.PERMISSION_NOT_FOUND
                                ));

        permission.setEnabled(true);

        Permission updatedPermission =
                permissionRepository.save(permission);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "PERMISSION_ACTIVATED",
                "Permission activated"
        );

        return permissionMapper.toResponse(
                updatedPermission
        );
    }

    @Override
    @Transactional
    public PermissionResponse deactivatePermission(
            Long permissionId
    ) {

        validateSuperAdminAccess();

        Permission permission =
                permissionRepository.findById(permissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.PERMISSION_NOT_FOUND
                                ));

        permission.setEnabled(false);

        Permission updatedPermission =
                permissionRepository.save(permission);

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "PERMISSION_DEACTIVATED",
                "Permission deactivated"
        );

        return permissionMapper.toResponse(
                updatedPermission
        );
    }

    @Override
    @Transactional
    public void deletePermission(Long permissionId) {

        validateSuperAdminAccess();

        Permission permission =
                permissionRepository.findById(permissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.PERMISSION_NOT_FOUND
                                ));

        permissionRepository.delete(permission);

        redisTemplate.delete(
                PERMISSION_CACHE_PREFIX + permissionId
        );

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "PERMISSION_DELETED",
                "Permission deleted permanently"
        );
    }

    @Override
    @Transactional
    public void softDeletePermission(
            Long permissionId
    ) {

        validateSuperAdminAccess();

        Permission permission =
                permissionRepository.findById(permissionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.PERMISSION_NOT_FOUND
                                ));

        permission.setIsDeleted(true);
        permission.setEnabled(false);

        permissionRepository.save(permission);

        redisTemplate.delete(
                PERMISSION_CACHE_PREFIX + permissionId
        );

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "PERMISSION_SOFT_DELETED",
                "Permission soft deleted"
        );
    }

    private void validateCreatePermissionRequest(
            CreatePermissionRequest request
    ) {

        if (!StringUtils.hasText(request.getName())) {

            throw new ValidationException(
                    MessageConstants.PERMISSION_NAME_REQUIRED
            );
        }

        if (!StringUtils.hasText(
                request.getDescription()
        )) {

            throw new ValidationException(
                    MessageConstants.PERMISSION_DESCRIPTION_REQUIRED
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
                                role.getRole()
                                        .getName()
                                        .equals(
                                                UserRoleType.SUPER_ADMIN.name()
                                        )
                        );

        if (!isSuperAdmin) {

            throw new ForbiddenException(
                    MessageConstants.ACCESS_DENIED
            );
        }
    }

    private void cachePermission(
            Permission permission
    ) {

        redisTemplate.opsForValue().set(
                PERMISSION_CACHE_PREFIX
                        + permission.getId(),
                permission,
                Duration.ofHours(12)
        );
    }
}