package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.request.RegisterDeviceRequest;
import com.lms.usermanagementservice.dto.request.UpdateDeviceRequest;
import com.lms.usermanagementservice.dto.request.UpdatePushTokenRequest;
import com.lms.usermanagementservice.dto.response.DeviceResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.entity.UserDevice;
import com.lms.usermanagementservice.enums.DeviceStatus;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.exception.DuplicateResourceException;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.repository.UserDeviceRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.DeviceService;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private static final String DEVICE_CACHE_PREFIX =
            "DEVICE:";

    private final UserDeviceRepository userDeviceRepository;

    private final SecurityUtil securityUtil;

    private final AuditLogService auditLogService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public DeviceResponse registerDevice(
            RegisterDeviceRequest request
    ) {

        log.info(LogConstants.DEVICE_REGISTER_INITIATED);

        validateRegisterRequest(request);

        User currentUser =
                securityUtil.getCurrentUser();

        boolean alreadyExists =
                userDeviceRepository
                        .existsByDeviceIdAndUserId(
                                request.getDeviceId(),
                                currentUser.getId()
                        );

        if (alreadyExists) {

            throw new DuplicateResourceException(
                    MessageConstants.DEVICE_ALREADY_REGISTERED
            );
        }

        UserDevice device = new UserDevice();

        device.setUser(currentUser);
        device.setDeviceId(request.getDeviceId());
        device.setDeviceName(request.getDeviceName());
        device.setDeviceType(request.getDeviceType());
        device.setPlatformType(request.getPlatformType());
        device.setPushToken(request.getPushToken());
        device.setStatus(DeviceStatus.ACTIVE);
        device.setLastLoginAt(LocalDateTime.now());

        UserDevice savedDevice =
                userDeviceRepository.save(device);

        cacheDevice(savedDevice);

        auditLogService.createAuditLog(
                currentUser.getId(),
                "DEVICE_REGISTERED",
                "User device registered"
        );

        log.info(LogConstants.DEVICE_REGISTER_SUCCESS);

        return mapToResponse(savedDevice);
    }

    @Override
    @Transactional
    public DeviceResponse updateDevice(
            Long deviceId,
            UpdateDeviceRequest request
    ) {

        UserDevice device =
                userDeviceRepository.findById(deviceId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.DEVICE_NOT_FOUND
                                ));

        validateDeviceOwnership(device);

        if (StringUtils.hasText(request.getDeviceName())) {
            device.setDeviceName(
                    request.getDeviceName()
            );
        }

        if (request.getDeviceType() != null) {
            device.setDeviceType(
                    request.getDeviceType()
            );
        }

        if (request.getPlatformType() != null) {
            device.setPlatformType(
                    request.getPlatformType()
            );
        }

        UserDevice updatedDevice =
                userDeviceRepository.save(device);

        cacheDevice(updatedDevice);

        auditLogService.createAuditLog(
                device.getUser().getId(),
                "DEVICE_UPDATED",
                "Device updated successfully"
        );

        return mapToResponse(updatedDevice);
    }

    @Override
    @Transactional
    public DeviceResponse updatePushToken(
            Long deviceId,
            UpdatePushTokenRequest request
    ) {

        UserDevice device =
                userDeviceRepository.findById(deviceId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.DEVICE_NOT_FOUND
                                ));

        validateDeviceOwnership(device);

        if (!StringUtils.hasText(
                request.getPushToken()
        )) {

            throw new ValidationException(
                    MessageConstants.PUSH_TOKEN_REQUIRED
            );
        }

        device.setPushToken(
                request.getPushToken()
        );

        UserDevice updatedDevice =
                userDeviceRepository.save(device);

        cacheDevice(updatedDevice);

        auditLogService.createAuditLog(
                device.getUser().getId(),
                "PUSH_TOKEN_UPDATED",
                "Push token updated"
        );

        return mapToResponse(updatedDevice);
    }

    @Override
    public DeviceResponse getDeviceById(
            Long deviceId
    ) {

        UserDevice device =
                userDeviceRepository.findById(deviceId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.DEVICE_NOT_FOUND
                                ));

        validateDeviceOwnership(device);

        return mapToResponse(device);
    }

    @Override
    public List<DeviceResponse> getCurrentUserDevices() {

        User currentUser =
                securityUtil.getCurrentUser();

        List<UserDevice> devices =
                userDeviceRepository.findAllByUserId(
                        currentUser.getId()
                );

        return devices.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PageResponse<DeviceResponse> getAllDevices(
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

        Page<UserDevice> devicePage =
                userDeviceRepository.findAll(pageable);

        List<DeviceResponse> responses =
                devicePage.getContent()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return PageResponse.<DeviceResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(devicePage.getTotalPages())
                .totalElements(devicePage.getTotalElements())
                .last(devicePage.isLast())
                .build();
    }

    @Override
    public PageResponse<DeviceResponse> searchDevices(
            String keyword,
            int page,
            int size
    ) {

        validateAdminAccess();

        Pageable pageable =
                PageRequest.of(page, size);

        Specification<UserDevice> specification =
                (root, query, criteriaBuilder) -> {

                    List<Predicate> predicates =
                            new ArrayList<>();

                    if (StringUtils.hasText(keyword)) {

                        predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("deviceName")
                                                ),
                                                "%" + keyword.toLowerCase() + "%"
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(
                                                        root.get("deviceId")
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

        Page<UserDevice> devicePage =
                userDeviceRepository.findAll(
                        specification,
                        pageable
                );

        List<DeviceResponse> responses =
                devicePage.getContent()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return PageResponse.<DeviceResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalPages(devicePage.getTotalPages())
                .totalElements(devicePage.getTotalElements())
                .last(devicePage.isLast())
                .build();
    }

    @Override
    @Transactional
    public DeviceResponse activateDevice(
            Long deviceId
    ) {

        UserDevice device =
                userDeviceRepository.findById(deviceId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.DEVICE_NOT_FOUND
                                ));

        validateDeviceOwnership(device);

        device.setStatus(DeviceStatus.ACTIVE);

        UserDevice updatedDevice =
                userDeviceRepository.save(device);

        auditLogService.createAuditLog(
                device.getUser().getId(),
                "DEVICE_ACTIVATED",
                "Device activated"
        );

        return mapToResponse(updatedDevice);
    }

    @Override
    @Transactional
    public DeviceResponse deactivateDevice(
            Long deviceId
    ) {

        UserDevice device =
                userDeviceRepository.findById(deviceId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.DEVICE_NOT_FOUND
                                ));

        validateDeviceOwnership(device);

        device.setStatus(DeviceStatus.INACTIVE);

        UserDevice updatedDevice =
                userDeviceRepository.save(device);

        auditLogService.createAuditLog(
                device.getUser().getId(),
                "DEVICE_DEACTIVATED",
                "Device deactivated"
        );

        return mapToResponse(updatedDevice);
    }

    @Override
    @Transactional
    public void removeDevice(Long deviceId) {

        UserDevice device =
                userDeviceRepository.findById(deviceId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.DEVICE_NOT_FOUND
                                ));

        validateDeviceOwnership(device);

        userDeviceRepository.delete(device);

        redisTemplate.delete(
                DEVICE_CACHE_PREFIX + deviceId
        );

        auditLogService.createAuditLog(
                device.getUser().getId(),
                "DEVICE_REMOVED",
                "Device removed"
        );
    }

    @Override
    @Transactional
    public void logoutDevice(Long deviceId) {

        UserDevice device =
                userDeviceRepository.findById(deviceId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.DEVICE_NOT_FOUND
                                ));

        validateDeviceOwnership(device);

        device.setStatus(DeviceStatus.LOGGED_OUT);
        device.setLastLogoutAt(LocalDateTime.now());

        userDeviceRepository.save(device);

        redisTemplate.delete(
                DEVICE_CACHE_PREFIX + deviceId
        );

        auditLogService.createAuditLog(
                device.getUser().getId(),
                "DEVICE_LOGOUT",
                "Device logout successful"
        );
    }

    @Override
    @Transactional
    public void logoutAllDevices(Long userId) {

        User currentUser =
                securityUtil.getCurrentUser();

        validateUserAccess(
                currentUser,
                userId
        );

        List<UserDevice> devices =
                userDeviceRepository.findAllByUserId(
                        userId
                );

        devices.forEach(device -> {

            device.setStatus(
                    DeviceStatus.LOGGED_OUT
            );

            device.setLastLogoutAt(
                    LocalDateTime.now()
            );

            redisTemplate.delete(
                    DEVICE_CACHE_PREFIX
                            + device.getId()
            );
        });

        userDeviceRepository.saveAll(devices);

        auditLogService.createAuditLog(
                userId,
                "ALL_DEVICES_LOGOUT",
                "All devices logged out"
        );
    }

    @Override
    public Boolean isDeviceRegistered(
            String deviceId,
            Long userId
    ) {

        validateUserAccess(securityUtil.getCurrentUser(), userId);

        return userDeviceRepository
                .existsByDeviceIdAndUserId(
                        deviceId,
                        userId
                );
    }

    private void validateRegisterRequest(
            RegisterDeviceRequest request
    ) {

        if (!StringUtils.hasText(
                request.getDeviceId()
        )) {

            throw new ValidationException(
                    MessageConstants.DEVICE_ID_REQUIRED
            );
        }

        if (!StringUtils.hasText(
                request.getDeviceName()
        )) {

            throw new ValidationException(
                    MessageConstants.DEVICE_NAME_REQUIRED
            );
        }

        if (request.getDeviceType() == null) {

            throw new ValidationException(
                    MessageConstants.DEVICE_TYPE_REQUIRED
            );
        }

        if (request.getPlatformType() == null) {

            throw new ValidationException(
                    MessageConstants.PLATFORM_TYPE_REQUIRED
            );
        }
    }

    private void validateDeviceOwnership(
            UserDevice device
    ) {

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

        if (!isAdmin
                && !device.getUser()
                .getId()
                .equals(currentUser.getId())) {

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

    private void validateUserAccess(
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

    private void cacheDevice(
            UserDevice device
    ) {

        redisTemplate.opsForValue().set(
                DEVICE_CACHE_PREFIX
                        + device.getId(),
                device,
                Duration.ofHours(12)
        );
    }

    private DeviceResponse mapToResponse(
            UserDevice device
    ) {

        return DeviceResponse.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .platformType(device.getPlatformType())
                .status(device.getStatus())
                .pushToken(device.getPushToken())
                .lastLoginAt(device.getLastLoginAt())
                .lastLogoutAt(device.getLastLogoutAt())
                .createdAt(device.getCreatedAt())
                .build();
    }
}
