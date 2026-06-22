package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.entity.OTP;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.OTPStatus;
import com.lms.usermanagementservice.enums.OTPType;
import com.lms.usermanagementservice.exception.OTPExpiredException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.repository.OTPRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.OTPService;
import com.lms.usermanagementservice.util.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import com.lms.usermanagementservice.util.OTPUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
// TODO: Redis is temporarily disabled by default. Enable the "redis" profile to load this service again.
//@Profile("redis")
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {

    private static final Integer MAX_OTP_ATTEMPTS = 5;
    private static final Integer OTP_EXPIRY_MINUTES = 5;
    private static final long OTP_RESEND_COOLDOWN_SECONDS = 60;
    private static final String OTP_CACHE_PREFIX = "OTP:";
    private static final String OTP_ATTEMPT_PREFIX = "OTP_ATTEMPT:";

    private final OTPRepository otpRepository;

    //private final OTPUtil otpUtil;
    

   // private final JavaMailSender javaMailSender;

  //  private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuditLogService auditLogService;

    @Override
    public String generateOtp() {

        return OTPUtil.generateNumericOtp(6);
    }

    @Override
    @Transactional
    public void generateAndSendOtp(User user, OTPType otpType) {

        log.info(LogConstants.OTP_GENERATION_STARTED);

        LocalDateTime now = LocalDateTime.now();
        if (user.getLastOtpSentAt() != null
                && !now.isAfter(user.getLastOtpSentAt().plusSeconds(OTP_RESEND_COOLDOWN_SECONDS))) {
            throw new ValidationException("OTP can only be resent after 60 seconds");
        }

        List<OTP> activeOtps = otpRepository.findAllByUserIdAndStatus(user.getId(), OTPStatus.PENDING);
        if (!activeOtps.isEmpty()) {
            activeOtps.forEach(active -> active.setStatus(OTPStatus.EXPIRED));
            otpRepository.saveAll(activeOtps);
        }

        String otpCode = generateOtp();

        OTP otp = new OTP();

        otp.setUser(user);
        otp.setEmail(user.getEmail());
        otp.setOtpCode(otpCode);
        otp.setOtpType(otpType);
        otp.setStatus(OTPStatus.PENDING);
        otp.setAttemptCount(0);
        otp.setExpiryTime(
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
        );

        otpRepository.save(otp);

        user.setLastOtpSentAt(now);

        cacheOtp(user.getEmail(), otpCode, otpType);

        sendOtpEmail(
                user.getEmail(),
                otpCode,
                otpType
        );

        auditLogService.createAuditLog(
                user.getId(),
                "OTP_GENERATED",
                "OTP generated for " + otpType.name()
        );

        log.info(LogConstants.OTP_GENERATION_COMPLETED);
    }

    @Override
    @Transactional
    public Boolean validateOtp(
            String email,
            String otpCode,
            OTPType otpType
    ) {

        OTP otp = otpRepository
                .findTopByEmailAndOtpCodeAndOtpTypeOrderByCreatedAtDesc(
                        email,
                        otpCode,
                        otpType
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.INVALID_OTP
                        ));

        if (otp.getStatus() == OTPStatus.VERIFIED) {
            throw new ValidationException(
                    MessageConstants.OTP_ALREADY_USED
            );
        }

        if (otp.getStatus() == OTPStatus.EXPIRED) {
            throw new OTPExpiredException(
                    MessageConstants.OTP_EXPIRED
            );
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {

            otp.setStatus(OTPStatus.EXPIRED);

            otpRepository.save(otp);

            throw new OTPExpiredException(
                    MessageConstants.OTP_EXPIRED
            );
        }

        int attempts = otp.getAttemptCount() + 1;
        otp.setAttemptCount(attempts);

        if (attempts > MAX_OTP_ATTEMPTS) {

            otp.setStatus(OTPStatus.EXPIRED);

            otpRepository.save(otp);

            throw new ValidationException(
                    MessageConstants.MAX_OTP_ATTEMPTS_EXCEEDED
            );
        }

        otp.setStatus(OTPStatus.VERIFIED);

        otpRepository.save(otp);

        clearOtpCache(email, otpType);

        auditLogService.createAuditLog(
                otp.getUser().getId(),
                "OTP_VERIFIED",
                "OTP validated successfully"
        );

        return true;
    }

    @Override
    @Transactional
    public void invalidateOtp(String email, OTPType otpType) {

        List<OTP> otpList =
                otpRepository.findAllByEmailAndOtpTypeAndStatus(
                        email,
                        otpType,
                        OTPStatus.PENDING
                );

        if (!CollectionUtils.isEmpty(otpList)) {

            otpList.forEach(otp -> otp.setStatus(OTPStatus.EXPIRED));

            otpRepository.saveAll(otpList);
        }

        clearOtpCache(email, otpType);
    }

    @Override
    @Transactional
    public void resendOtp(User user, OTPType otpType) {

        invalidateOtp(
                user.getEmail(),
                otpType
        );

        generateAndSendOtp(
                user,
                otpType
        );

        auditLogService.createAuditLog(
                user.getId(),
                "OTP_RESENT",
                "OTP resent successfully"
        );
    }

    @Override
    public Integer getRemainingAttempts(
            String email,
            OTPType otpType
    ) {

        String key = OTP_ATTEMPT_PREFIX
                + email
                + ":"
                + otpType.name();

        Object attempts = redisTemplate.opsForValue().get(key);

        Integer usedAttempts =
                attempts != null
                        ? Integer.parseInt(attempts.toString())
                        : 0;

        return MAX_OTP_ATTEMPTS - usedAttempts;
    }

    @Override
    public Boolean isOtpExpired(
            String email,
            String otpCode
    ) {

        OTP otp = otpRepository
                .findTopByEmailAndOtpCodeOrderByCreatedAtDesc(
                        email,
                        otpCode
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.OTP_NOT_FOUND
                        ));

        return otp.getExpiresAt()
                .isBefore(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void clearExpiredOtps() {

        List<OTP> expiredOtps =
                otpRepository.findAllByExpiryTimeBeforeAndStatus(
                        LocalDateTime.now(),
                        OTPStatus.PENDING
                );

        if (CollectionUtils.isEmpty(expiredOtps)) {
            return;
        }

        expiredOtps.forEach(
                otp -> otp.setStatus(OTPStatus.EXPIRED)
        );

        otpRepository.saveAll(expiredOtps);

        log.info("Expired OTP cleanup completed");
    }

    private void sendOtpEmail(
            String email,
            String otpCode,
            OTPType otpType
    ) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); 
            message.setTo(email);
            message.setSubject(buildSubject(otpType));
            message.setText(buildOtpMessage(otpCode, otpType));

            javaMailSender.send(message);

            log.info("OTP email sent successfully");

        } catch (Exception ex) {

            log.error("Failed to send OTP email",ex);

            throw new ValidationException(
                    MessageConstants.OTP_SEND_FAILED
            );
        }
    }

    private String buildSubject(OTPType otpType) {

        return switch (otpType) {

            case EMAIL_VERIFICATION ->
                    "LMS Telugu - Email Verification OTP";

            case PASSWORD_RESET ->
                    "LMS Telugu - Password Reset OTP";

            case LOGIN_VERIFICATION ->
                    "LMS Telugu - Login Verification OTP";

            default ->
                    "LMS Telugu OTP Verification";
        };
    }

    private String buildOtpMessage(
            String otpCode,
            OTPType otpType
    ) {

        return """
                Dear User,
                                
                Your OTP for %s is: %s
                                
                This OTP is valid for 5 minutes.
                                
                Please do not share this OTP with anyone.
                                
                LMS Telugu Security Team
                """.formatted(
                otpType.name(),
                otpCode
        );
    }

    private void cacheOtp(
            String email,
            String otpCode,
            OTPType otpType
    ) {

        String key = OTP_CACHE_PREFIX
                + email
                + ":"
                + otpType.name();

        redisTemplate.opsForValue().set(
                key,
                otpCode,
                OTP_EXPIRY_MINUTES,
                TimeUnit.MINUTES
        );
    }

    private void clearOtpCache(
            String email,
            OTPType otpType
    ) {

        String otpKey = OTP_CACHE_PREFIX
                + email
                + ":"
                + otpType.name();

        String attemptKey = OTP_ATTEMPT_PREFIX
                + email
                + ":"
                + otpType.name();

        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptKey);
    }

    private Integer incrementOtpAttempt(
            String email,
            OTPType otpType
    ) {

        String key = OTP_ATTEMPT_PREFIX
                + email
                + ":"
                + otpType.name();

        Object value = redisTemplate.opsForValue().get(key);

        Integer attempts =
                value != null
                        ? Integer.parseInt(value.toString())
                        : 0;

        attempts++;

        redisTemplate.opsForValue().set(
                key,
                attempts,
                OTP_EXPIRY_MINUTES,
                TimeUnit.MINUTES
        );

        return attempts;
    }
}
