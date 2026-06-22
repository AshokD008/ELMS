//package com.lms.usermanagementservice.service.impl;
//
//import com.lms.usermanagementservice.entity.User;
//import com.lms.usermanagementservice.enums.OTPType;
//import com.lms.usermanagementservice.exception.ValidationException;
//import com.lms.usermanagementservice.service.OTPService;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//
//@Service
//// TODO: Redis is temporarily disabled. Remove this service when Redis-backed OTP is re-enabled.
//@Profile("!redis")
//public class RedisDisabledOTPServiceImpl implements OTPService {
//
//    private static final String REDIS_DISABLED_MESSAGE =
//            "OTP service is temporarily disabled because Redis is not available";
//
//    @Override
//    public String generateOtp() {
//
//        throw redisDisabled();
//    }
//
//    @Override
//    public void generateAndSendOtp(
//            User user,
//            OTPType otpType
//    ) {
//
//        throw redisDisabled();
//    }
//
//    @Override
//    public Boolean validateOtp(
//            String email,
//            String otpCode,
//            OTPType otpType
//    ) {
//
//        throw redisDisabled();
//    }
//
//    @Override
//    public void invalidateOtp(
//            String email,
//            OTPType otpType
//    ) {
//
//        throw redisDisabled();
//    }
//
//    @Override
//    public void resendOtp(
//            User user,
//            OTPType otpType
//    ) {
//
//        throw redisDisabled();
//    }
//
//    @Override
//    public Integer getRemainingAttempts(
//            String email,
//            OTPType otpType
//    ) {
//
//        return 0;
//    }
//
//    @Override
//    public Boolean isOtpExpired(
//            String email,
//            String otpCode
//    ) {
//
//        return true;
//    }
//
//    @Override
//    public void clearExpiredOtps() {
//        // Redis-backed OTP cleanup is intentionally disabled while Redis is unavailable.
//    }
//
//    private ValidationException redisDisabled() {
//
//        return new ValidationException(REDIS_DISABLED_MESSAGE);
//    }
//}
