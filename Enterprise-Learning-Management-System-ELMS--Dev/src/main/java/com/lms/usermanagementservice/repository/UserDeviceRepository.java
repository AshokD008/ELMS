package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.UserDevice;
import com.lms.usermanagementservice.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long>, JpaSpecificationExecutor<UserDevice> {

    Optional<UserDevice> findByDeviceUniqueId(String deviceUniqueId);

    boolean existsByDeviceUniqueIdAndUserId(String deviceUniqueId, Long userId);

    default boolean existsByDeviceIdAndUserId(String deviceId, Long userId) {
        return existsByDeviceUniqueIdAndUserId(deviceId, userId);
    }

    List<UserDevice> findByUserId(Long userId);

    default List<UserDevice> findAllByUserId(Long userId) {
        return findByUserId(userId);
    }

    List<UserDevice> findByDeviceStatus(DeviceStatus deviceStatus);
}
