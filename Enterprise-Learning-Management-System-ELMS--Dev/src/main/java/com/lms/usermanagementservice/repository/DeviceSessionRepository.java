package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {

    List<DeviceSession> findByUserDeviceId(Long userDeviceId);
}