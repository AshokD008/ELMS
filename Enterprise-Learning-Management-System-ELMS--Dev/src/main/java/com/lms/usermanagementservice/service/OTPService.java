package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.OTPType;

public interface OTPService {

    String generateOtp();

    void generateAndSendOtp(User user, OTPType otpType);

    Boolean validateOtp(String email, String otpCode, OTPType otpType);

    void invalidateOtp(String email, OTPType otpType);

    void resendOtp(User user, OTPType otpType);

    Integer getRemainingAttempts(String email, OTPType otpType);

    Boolean isOtpExpired(String email, String otpCode);

    void clearExpiredOtps();
}