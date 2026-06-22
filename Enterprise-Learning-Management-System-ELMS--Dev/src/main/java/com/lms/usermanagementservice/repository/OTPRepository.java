package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.OTP;
import com.lms.usermanagementservice.enums.OTPStatus;
import com.lms.usermanagementservice.enums.OTPType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OTPRepository extends JpaRepository<OTP, Long> {

    Optional<OTP> findTopByEmailAndOtpTypeOrderByCreatedAtDesc(
            String email,
            OTPType otpType
    );

    Optional<OTP> findTopByEmailAndOtpCodeOrderByCreatedAtDesc(
            String email,
            String otpCode
    );

    Optional<OTP> findTopByEmailAndOtpCodeAndOtpTypeOrderByCreatedAtDesc(
            String email,
            String otpCode,
            OTPType otpType
    );

    List<OTP> findByStatus(OTPStatus status);

    List<OTP> findAllByEmailAndOtpTypeAndStatus(String email, OTPType otpType, OTPStatus status);

    List<OTP> findAllByUserIdAndStatus(Long userId, OTPStatus status);

    List<OTP> findAllByExpiresAtBeforeAndStatus(java.time.LocalDateTime expiresAt, OTPStatus status);

    default List<OTP> findAllByExpiryTimeBeforeAndStatus(java.time.LocalDateTime expiryTime, OTPStatus status) {
        return findAllByExpiresAtBeforeAndStatus(expiryTime, status);
    }
}
