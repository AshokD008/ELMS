package com.lms.usermanagementservice.service.impl;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.request.ForgotPasswordRequest;
import com.lms.usermanagementservice.dto.request.LoginRequest;
import com.lms.usermanagementservice.dto.request.LogoutRequest;
import com.lms.usermanagementservice.dto.request.RefreshTokenRequest;
import com.lms.usermanagementservice.dto.request.RegisterRequest;
import com.lms.usermanagementservice.dto.request.ResendOTPRequest;
import com.lms.usermanagementservice.dto.request.ResetPasswordRequest;
import com.lms.usermanagementservice.dto.request.VerifyOTPRequest;
import com.lms.usermanagementservice.dto.response.AuthResponse;
import com.lms.usermanagementservice.dto.response.LoginResponse;
import com.lms.usermanagementservice.dto.response.RefreshTokenResponse;
import com.lms.usermanagementservice.dto.response.TokenResponse;
import com.lms.usermanagementservice.entity.College;
import com.lms.usermanagementservice.entity.CollegeAdminProfile;
import com.lms.usermanagementservice.entity.FacultyProfile;
import com.lms.usermanagementservice.entity.OTP;
import com.lms.usermanagementservice.entity.PasswordHistory;
import com.lms.usermanagementservice.entity.RefreshToken;
import com.lms.usermanagementservice.entity.Role;
import com.lms.usermanagementservice.entity.Session;
import com.lms.usermanagementservice.entity.StudentProfile;
import com.lms.usermanagementservice.entity.SuperAdminProfile;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.entity.UserRole;
import com.lms.usermanagementservice.enums.AccountType;
import com.lms.usermanagementservice.enums.ApprovalStage;
import com.lms.usermanagementservice.enums.OTPStatus;
import com.lms.usermanagementservice.enums.OTPType;
import com.lms.usermanagementservice.enums.SessionStatus;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.enums.UserStatus;
import com.lms.usermanagementservice.exception.DuplicateResourceException;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.InvalidTokenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.UnauthorizedException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.mapper.UserMapper;
import com.lms.usermanagementservice.repository.CollegeAdminProfileRepository;
import com.lms.usermanagementservice.repository.CollegeRepository;
import com.lms.usermanagementservice.repository.FacultyProfileRepository;
import com.lms.usermanagementservice.repository.OTPRepository;
import com.lms.usermanagementservice.repository.PasswordHistoryRepository;
import com.lms.usermanagementservice.repository.RefreshTokenRepository;
import com.lms.usermanagementservice.repository.RoleRepository;
import com.lms.usermanagementservice.repository.SessionRepository;
import com.lms.usermanagementservice.repository.StudentProfileRepository;
import com.lms.usermanagementservice.repository.SuperAdminProfileRepository;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.repository.UserRoleRepository;
import com.lms.usermanagementservice.security.jwt.JwtTokenProvider;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.AuthService;
import com.lms.usermanagementservice.service.IdGenerationService;
import com.lms.usermanagementservice.service.LoginAttemptService;
import com.lms.usermanagementservice.service.OTPService;
import com.lms.usermanagementservice.util.SecurityUtil;
import com.lms.usermanagementservice.validator.EmailValidator;
import com.lms.usermanagementservice.validator.PasswordValidator;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final OTPRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SessionRepository sessionRepository;
    private final SuperAdminProfileRepository superAdminProfileRepository;
    private final CollegeRepository collegeRepository;
    private final CollegeAdminProfileRepository collegeAdminProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final IdGenerationService idGenerationService;

    private final UserMapper userMapper;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailValidator emailValidator;

    private final OTPService otpService;
    private final AuditLogService auditLogService;
    private final LoginAttemptService loginAttemptService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        log.info(LogConstants.AUTH_REGISTER_INITIATED);

        validateRegisterRequest(request);
        if (request.getRole() == null) {
            throw new ValidationException("Role is required");
        }
        if (request.getRole() == UserRoleType.FACULTY && request.getCollegeAdminId() == null) {
            throw new ValidationException("collegeAdminId is required for faculty registration");
        }
        if (request.getRole() == UserRoleType.COLLEGE_ADMIN && request.getCollegeAdminId() != null) {
            throw new ValidationException("College admins cannot be mapped to another college admin");
        }
//        if (request.getCollegeAdminId() != null) {
//        	
//            User owner = userRepository.findById(request.getCollegeAdminId())
//                    .orElseThrow(() -> new ResourceNotFoundException("College admin not found"));
if (StringUtils.hasText(request.getCollegeAdminId())) {
            Long collegeAdminId = Long.parseLong(request.getCollegeAdminId());

            User owner = userRepository.findById(collegeAdminId)
                    .orElseThrow(() -> new ResourceNotFoundException("College admin not found"));
            if (owner.getAccountType() != AccountType.COLLEGE_ADMIN) {
                throw new ValidationException("collegeAdminId must identify a college admin");
            }
        }

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(MessageConstants.EMAIL_ALREADY_EXISTS);
        }

        if (StringUtils.hasText(request.getPhoneNumber())
                && userRepository.existsByMobileNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException(MessageConstants.PHONE_ALREADY_EXISTS);
        }
        Role defaultRole;

      

        if (request.getRole() == UserRoleType.SUPER_ADMIN) {

            long superAdminCount = userRoleRepository.countByRole_RoleName(UserRoleType.SUPER_ADMIN.name())
                    + userRoleRepository.countByRole_RoleName("ROLE_" + UserRoleType.SUPER_ADMIN.name());

            if (superAdminCount > 0) {

                throw new ValidationException(
                        "SUPER_ADMIN already exists"
                );
            }
        }

        defaultRole = resolveRole(request.getRole());

        User user = userMapper.toEntity(request);

      //  user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(email);
        user.setUsername(email);

        user.setMobileNumber(request.getPhoneNumber());

//        user.setStatus(UserStatus.PENDING_VERIFICATION);
//        user.setEmailVerified(false);
        user.setMobileVerified(false);
        user.setAccountNonLocked(true);
        user.setEnabled(true);
        user.setIsDeleted(false);
        
        
        		if (request.getRole() == UserRoleType.SUPER_ADMIN) {

        		    user.setStatus(UserStatus.ACTIVE);

        		    user.setApprovalStage(
        		            ApprovalStage.SUPER_ADMIN_APPROVED
        		    );

        		    user.setEmailVerified(true);

        		} else {

        		    user.setStatus(
        		            UserStatus.PENDING_VERIFICATION
        		    );

        		    user.setApprovalStage(
        		            ApprovalStage.NONE
        		    );

        		    user.setEmailVerified(false);
        		}
        		

     // ADD THIS LINE
        user.setAccountType(
                AccountType.valueOf(request.getRole().name())
        );

        College college = null;
        if (request.getCollegeId() != null) {
            college = collegeRepository.findById(request.getCollegeId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.COLLEGE_NOT_FOUND));
            user.setCollege(college);
        } else if (request.getCollegeAdminId() != null) {
//            college = userRepository.findById(request.getCollegeAdminId())
//                    .map(User::getCollege)
//                    .orElse(null);
        	Long collegeAdminId = Long.parseLong(request.getCollegeAdminId());

        	college = userRepository.findById(collegeAdminId)
        	        .map(User::getCollege)
        	        .orElse(null);
            user.setCollege(college);
        }

        User savedUser = userRepository.save(user);
        if (request.getRole() == UserRoleType.SUPER_ADMIN) {

            SuperAdminProfile profile =
                    new SuperAdminProfile();

            profile.setUser(savedUser);

            profile.setDepartment("Administration");

            profile.setAccessLevel("ROOT");

            profile.setCreatedAt(LocalDateTime.now());

            superAdminProfileRepository.save(profile);
        }
        else if (request.getRole() == UserRoleType.COLLEGE_ADMIN) {

            CollegeAdminProfile profile = new CollegeAdminProfile();
            profile.setUser(savedUser);
            profile.setCollege(null);
            profile.setDesignation(request.getDesignation());
            profile.setEmployeeId(idGenerationService.nextCollegeAdminId());
            profile.setJoiningDate(LocalDateTime.now());

            collegeAdminProfileRepository.save(profile);
        }
        else if (request.getRole() == UserRoleType.FACULTY) {
            FacultyProfile profile = new FacultyProfile();
            profile.setUser(savedUser);
            profile.setCollege(college);
//            profile.setFacultyId(idGenerationService.nextFacultyId(request.getCollegeAdminId()));
            profile.setFacultyId(idGenerationService.nextFacultyId(Long.parseLong(request.getCollegeAdminId())));
            profile.setDesignation(request.getDesignation());
            profile.setDepartment(request.getDepartment());
            facultyProfileRepository.save(profile);
        }
        else if (request.getRole() == UserRoleType.STUDENT) {

            StudentProfile profile = new StudentProfile();
            profile.setUser(savedUser);
            profile.setCollege(college);
            profile.setStudentId(
                    idGenerationService.nextIndependentStudentId(savedUser.getFirstName())
            );
            profile.setAdmissionNumber(
                    StringUtils.hasText(request.getAdmissionNumber())
                            ? request.getAdmissionNumber()
                            : profile.getStudentId()
            );
            profile.setRollNumber(
                    StringUtils.hasText(request.getStudentRollNumber())
                            ? request.getStudentRollNumber()
                            : profile.getStudentId()
            );
            profile.setDepartment(request.getDepartment());
            profile.setYear(request.getYear());
            profile.setSemester(request.getSemester());
            profile.setSection(request.getSection());
            profile.setAdmissionDate(LocalDateTime.now());

            studentProfileRepository.save(profile);
        }
     

        String accessToken = null;
        String refreshToken = null;
        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(defaultRole);

        userRoleRepository.save(userRole);

        if (request.getRole() == UserRoleType.SUPER_ADMIN) {

            accessToken = jwtTokenProvider.generateAccessToken(savedUser);
            refreshToken = jwtTokenProvider.generateRefreshToken(savedUser);

            saveRefreshToken(savedUser, refreshToken);
            createSession(savedUser, accessToken);
            cacheUserSession(savedUser.getId(), accessToken);
        }

        if (request.getRole() != UserRoleType.SUPER_ADMIN) {

            otpService.generateAndSendOtp(
                    savedUser,
                    OTPType.EMAIL_VERIFICATION
            );
        }

        auditLogService.createAuditLog(
                savedUser.getId(),
                "USER_REGISTERED",
                "User registration completed successfully"
        );

        log.info(LogConstants.AUTH_REGISTER_SUCCESS);

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getMobileNumber())
                .userStatus(savedUser.getStatus())
                .roles(List.of(request.getRole()))
                .user(userMapper.toProfileResponse(savedUser))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .success(true)
                .message("Registration successful")
                .build();
    }

    @Override
    public AuthResponse registerSuperAdmin(RegisterRequest request) {
        request.setRole(UserRoleType.SUPER_ADMIN);
        return register(request);
    }

    @Override
    public AuthResponse registerCollegeAdmin(RegisterRequest request) {
        request.setRole(UserRoleType.COLLEGE_ADMIN);
        return register(request);
    }

    @Override
    public AuthResponse registerFaculty(RegisterRequest request) {
        request.setRole(UserRoleType.FACULTY);
        return register(request);
    }

    @Override
    public AuthResponse registerStudent(RegisterRequest request) {
        request.setRole(UserRoleType.STUDENT);
        return register(request);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        log.info(LogConstants.AUTH_LOGIN_INITIATED);

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findWithRolesAndCollegeByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException(MessageConstants.INVALID_CREDENTIALS));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ForbiddenException(MessageConstants.ACCOUNT_DELETED);
        }

        LocalDateTime now = LocalDateTime.now();
        if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(now)) {
            throw new ForbiddenException("Account locked for 15 minutes after repeated login failures");
        }
        if (user.getLockoutUntil() != null) {
            user.setLockoutUntil(null);
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
        }

        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new ForbiddenException(MessageConstants.ACCOUNT_BLOCKED);
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ForbiddenException("Please verify OTP first");
        }

        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new ForbiddenException(waitingForApprovalMessage(user));
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException(MessageConstants.ACCOUNT_INACTIVE);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailure(user.getId());
            throw new UnauthorizedException(MessageConstants.INVALID_CREDENTIALS);
        }
        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    request.getPassword()
                            )
                    );

        } catch (BadCredentialsException ex) {

            log.error("Invalid login credentials");

            loginAttemptService.recordFailure(user.getId());

            throw new UnauthorizedException(
                    MessageConstants.INVALID_CREDENTIALS
            );
        }

        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setAccountLocked(false);
        userRepository.save(user);

        otpService.generateAndSendOtp(
                user,
                OTPType.LOGIN_VERIFICATION
        );

        auditLogService.createAuditLog(
                user.getId(),
                "LOGIN_OTP_SENT",
                "Login OTP sent successfully"
        );

        return LoginResponse.builder()
                .success(true)
                .message("Login OTP sent to your email")
                .build();
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() ->
                        new InvalidTokenException(MessageConstants.INVALID_REFRESH_TOKEN));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {

            refreshTokenRepository.delete(refreshToken);

            throw new InvalidTokenException(
                    MessageConstants.REFRESH_TOKEN_EXPIRED
            );
        }

        User user = refreshToken.getUser();

        assertCanIssueToken(user);

     String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        refreshToken.setToken(newRefreshToken);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(30));

        refreshTokenRepository.save(refreshToken);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }

  
    		@Override
    		@Transactional
    		public TokenResponse verifyOtp(VerifyOTPRequest request) {

    		    OTP otp = otpRepository
    		            .findTopByEmailAndOtpCodeAndOtpTypeOrderByCreatedAtDesc(
    		                    request.getEmail(),
    		                    request.getOtp(),
    		                    request.getOtpType()
    		            )
    		            .orElseThrow(() ->
    		                    new ResourceNotFoundException(
    		                            MessageConstants.OTP_NOT_FOUND
    		                    ));

    		    // OTP ALREADY USED

    		    if (otp.getStatus() == OTPStatus.VERIFIED) {

    		        throw new ValidationException(
    		                MessageConstants.OTP_ALREADY_USED
    		        );
    		    }

    		    // OTP EXPIRED

    		    if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {

    		        throw new ValidationException(
    		                MessageConstants.OTP_EXPIRED
    		        );
    		    }

    		    User user = otp.getUser();

    		    // LOGIN OTP FLOW

    		    if (request.getOtpType()
    		            == OTPType.LOGIN_VERIFICATION) {

    		        assertCanIssueToken(user);

    		        String accessToken =
    		                jwtTokenProvider.generateAccessToken(user);

    		        String refreshToken =
    		                jwtTokenProvider.generateRefreshToken(user);

    		        saveRefreshToken(user, refreshToken);

    		        createSession(user, accessToken);

    		        cacheUserSession(user.getId(), accessToken);

    		        otp.setStatus(OTPStatus.VERIFIED);

    		        otpRepository.save(otp);

    		        return TokenResponse.builder()
    		                .success(true)
    		                .message("Login successful")
    		                .accessToken(accessToken)
    		                .refreshToken(refreshToken)
    		                .tokenType("Bearer")
    		                .expiresIn(86400000L)
    		                .build();
    		    }

    		    // EMAIL VERIFICATION FLOW

    		    user.setEmailVerified(true);

                user.setStatus(UserStatus.PENDING_VERIFICATION);

                user.setApprovalStage(ApprovalStage.NONE);

    		    userRepository.save(user);

    		    otp.setStatus(OTPStatus.VERIFIED);

    		    otpRepository.save(otp);

    		    auditLogService.createAuditLog(
    		            user.getId(),
    		            "OTP_VERIFIED",
    		            "OTP verified successfully"
    		    );

    		    return TokenResponse.builder()
    		            .message(
    		                    MessageConstants.OTP_VERIFIED_SUCCESSFULLY
    		            )
    		            .success(true)
    		            .build();
    		}

    @Override
    public TokenResponse verifyLoginOtp(VerifyOTPRequest request) {
        if (request.getOtpType() != OTPType.LOGIN_VERIFICATION) {
            throw new ValidationException("LOGIN_VERIFICATION OTP type is required");
        }
        return verifyOtp(request);
    }
    		

    @Override
    @Transactional
    public TokenResponse resendOtp(ResendOTPRequest request) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ValidationException(
                    MessageConstants.EMAIL_ALREADY_VERIFIED
            );
        }

        otpService.generateAndSendOtp(
                user,
                OTPType.EMAIL_VERIFICATION
        );

        return TokenResponse.builder()
                .message(MessageConstants.OTP_SENT_SUCCESSFULLY)
                .success(true)
                .build();
    }

    @Override
    @Transactional
    public TokenResponse forgotPassword(ForgotPasswordRequest request) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException(MessageConstants.ACCOUNT_INACTIVE);
        }

        otpService.generateAndSendOtp(
                user,
                OTPType.PASSWORD_RESET
        );

        auditLogService.createAuditLog(
                user.getId(),
                "FORGOT_PASSWORD",
                "Password reset OTP sent"
        );

        return TokenResponse.builder()
                .message(MessageConstants.PASSWORD_RESET_OTP_SENT)
                .success(true)
                .build();
    }

  
    		@Override
    		@Transactional
    		public TokenResponse resetPassword(ResetPasswordRequest request) {

    		    String email = request.getEmail()
    		            .trim()
    		            .toLowerCase(Locale.ROOT);

    		    User user = userRepository.findByEmail(email)
    		            .orElseThrow(() ->
    		                    new ResourceNotFoundException(
    		                            MessageConstants.USER_NOT_FOUND
    		                    ));

    		    OTP otp = otpRepository
    		            .findTopByEmailAndOtpCodeAndOtpTypeOrderByCreatedAtDesc(
    		                    request.getEmail(),
    		                    request.getOtp(),
    		                    OTPType.PASSWORD_RESET
    		            )
    		            .orElseThrow(() ->
    		                    new ValidationException(
    		                            MessageConstants.INVALID_OTP
    		                    ));

    		    // OTP ALREADY USED CHECK

    		    if (otp.getStatus() == OTPStatus.VERIFIED) {

    		        throw new ValidationException(
    		                MessageConstants.OTP_ALREADY_USED
    		        );
    		    }

    		    // OTP EXPIRED CHECK

    		    if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {

    		        throw new ValidationException(
    		                MessageConstants.OTP_EXPIRED
    		        );
    		    }

    		    PasswordValidator.validate(
    		            request.getNewPassword()
    		    );

                List<PasswordHistory> recentPasswords =
                        passwordHistoryRepository.findTop3ByUserIdOrderByCreatedAtDesc(user.getId());
                if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())
                        || recentPasswords.stream().anyMatch(history ->
                        passwordEncoder.matches(request.getNewPassword(), history.getPasswordHash()))) {
                    throw new ValidationException("New password cannot match any of the last 3 passwords");
                }

                passwordHistoryRepository.save(PasswordHistory.builder()
                        .user(user)
                        .passwordHash(user.getPassword())
                        .build());

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                user.setPasswordChangedAt(LocalDateTime.now());

    		    userRepository.save(user);

                List<PasswordHistory> histories =
                        passwordHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
                if (histories.size() > 3) {
                    passwordHistoryRepository.deleteAll(histories.subList(3, histories.size()));
                }

    		    otp.setStatus(OTPStatus.VERIFIED);

    		    otpRepository.save(otp);

    		    invalidateAllUserSessions(user.getId());

    		    auditLogService.createAuditLog(
    		            user.getId(),
    		            "PASSWORD_RESET",
    		            "Password reset completed"
    		    );

    		    return TokenResponse.builder()
    		            .message(
    		                    MessageConstants.PASSWORD_RESET_SUCCESS
    		            )
    		            .success(true)
    		            .build();
    		}
    		

    @Override
    @Transactional
    public TokenResponse logout(LogoutRequest request) {

        User currentUser = SecurityUtil.getCurrentUser();

        Session session = sessionRepository
                .findBySessionToken(request.getAccessToken())
                .orElseThrow(() ->
                        new ResourceNotFoundException(MessageConstants.SESSION_NOT_FOUND));

        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(MessageConstants.ACCESS_DENIED);
        }

        session.setStatus(SessionStatus.LOGGED_OUT);
        session.setLogoutAt(LocalDateTime.now());

        sessionRepository.save(session);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException(MessageConstants.INVALID_REFRESH_TOKEN));
        if (!refreshToken.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(MessageConstants.ACCESS_DENIED);
        }
        refreshTokenRepository.delete(refreshToken);

        redisTemplate.delete(
                "SESSION:" + currentUser.getId()
        );

        auditLogService.createAuditLog(
                currentUser.getId(),
                "USER_LOGOUT",
                "User logout successful"
        );

        return TokenResponse.builder()
                .message(MessageConstants.LOGOUT_SUCCESS)
                .success(true)
                .build();
    }

    @Override
    @Transactional
    public TokenResponse logoutAllDevices(Long userId) {

        User currentUser = SecurityUtil.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            throw new ForbiddenException(MessageConstants.ACCESS_DENIED);
        }

        List<Session> sessions =
                sessionRepository.findAllByUserId(userId);

        sessions.forEach(session -> {
            session.setStatus(SessionStatus.LOGGED_OUT);
            session.setLogoutAt(LocalDateTime.now());
        });

        sessionRepository.saveAll(sessions);

        refreshTokenRepository.deleteAllByUserId(userId);

        redisTemplate.delete("SESSION:" + userId);

        auditLogService.createAuditLog(
                userId,
                "LOGOUT_ALL_DEVICES",
                "All sessions terminated"
        );

        return TokenResponse.builder()
                .message(MessageConstants.ALL_DEVICES_LOGOUT_SUCCESS)
                .success(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUserProfile() {

        Long currentUserId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findWithRolesAndCollegeById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        return AuthResponse.builder()
                .success(true)
                .message(MessageConstants.USER_FETCH_SUCCESS)
                .data(userMapper.toProfileResponse(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean validateAccessToken(String token) {

        try {

            return jwtTokenProvider.validateToken(token);

        } catch (Exception ex) {

            log.error("Access token validation failed");

            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean validateRefreshToken(String refreshToken) {

        return refreshTokenRepository.existsByToken(refreshToken);
    }

    private void validateRegisterRequest(RegisterRequest request) {

        if (!StringUtils.hasText(request.getEmail())) {
            throw new ValidationException(
                    MessageConstants.EMAIL_REQUIRED
            );
        }

        if (!emailValidator.isValid(request.getEmail())) {
            throw new ValidationException(
                    MessageConstants.INVALID_EMAIL
            );
        }

        PasswordValidator.validate(request.getPassword());

        if (!StringUtils.hasText(request.getFirstName())) {
            throw new ValidationException(
                    MessageConstants.FIRST_NAME_REQUIRED
            );
        }

        if (!StringUtils.hasText(request.getLastName())) {
            throw new ValidationException(
                    MessageConstants.LAST_NAME_REQUIRED
            );
        }
    }

    private Role resolveRole(UserRoleType roleType) {

        String roleName = roleType.name();

        return roleRepository.findByRoleName(roleName)
                .or(() -> roleRepository.findByRoleName("ROLE_" + roleName))
                .orElseGet(() -> {

                    Role newRole = new Role();
                    newRole.setRoleName(roleName);
                    newRole.setDescription(roleName + " Role");
                    newRole.setEnabled(true);
                    newRole.setIsDeleted(false);

                    return roleRepository.save(newRole);
                });
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(30)
        );

        refreshTokenRepository.save(refreshToken);
    }


    private void createSession(User user, String accessToken) {

        Session session = new Session();

        session.setUser(user);
        session.setSessionToken(accessToken);
        session.setSessionId(UUID.randomUUID().toString());
        session.setIpAddress(httpServletRequest.getRemoteAddr());
        session.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        session.setStatus(SessionStatus.ACTIVE);
        session.setLoginAt(LocalDateTime.now());

        sessionRepository.save(session);
    }

    private void cacheUserSession(Long userId, String token) {

        redisTemplate.opsForValue().set(
                "SESSION:" + userId,
                token,
                30,
                TimeUnit.DAYS
        );
    }

    private void invalidateAllUserSessions(Long userId) {

        List<Session> sessions =
                sessionRepository.findAllByUserId(userId);

        sessions.forEach(session -> {
            session.setStatus(SessionStatus.LOGGED_OUT);
            session.setLogoutAt(LocalDateTime.now());
        });

        sessionRepository.saveAll(sessions);

        refreshTokenRepository.deleteAllByUserId(userId);

        redisTemplate.delete("SESSION:" + userId);
    }

    private void assertCanIssueToken(User user) {

        if (!Boolean.TRUE.equals(user.getEmailVerified())
                || user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new ForbiddenException("Please verify OTP first");
        }

        
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new ForbiddenException(MessageConstants.ACCOUNT_BLOCKED);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException(MessageConstants.ACCOUNT_INACTIVE);
        }
    }

    
    		private String waitingForApprovalMessage(User user) {

    		    if (user.getApprovalStage()
    		            == ApprovalStage.COLLEGE_ADMIN_APPROVED) {

    		        return "Waiting for Super Admin approval";
    		    }

    		    return switch (user.getAccountType()) {

    		        case COLLEGE_ADMIN ->
    		                "Waiting for Super Admin approval";

    		        case FACULTY, STAFF, STUDENT ->
    		                "Waiting for approval";

    		        default ->
    		                "Waiting for Super Admin approval";
    		    };
    		}
    		
    				@Override
    				@Transactional
    				public TokenResponse approveUser(Long userId) {

    				    User user = userRepository.findById(userId)
    				            .orElseThrow(() ->
    				                    new ResourceNotFoundException(
    				                            MessageConstants.USER_NOT_FOUND
    				                    ));

    				    user.setStatus(UserStatus.ACTIVE);

    				    user.setApprovalStage(
    				            ApprovalStage.SUPER_ADMIN_APPROVED
    				    );

    				    user.setApprovedBySuperAdmin(
    				            SecurityUtil.getCurrentUserId()
    				    );

    				    user.setSuperAdminApprovedAt(
    				            LocalDateTime.now()
    				    );

    				    userRepository.save(user);

    				    auditLogService.createAuditLog(
    				            user.getId(),
    				            "USER_APPROVED",
    				            "User approved by Super Admin"
    				    );

    				    return TokenResponse.builder()
    				            .success(true)
    				            .message("User approved successfully")
    				            .build();
    				}
    				
    						@Override
    						@Transactional
    						public TokenResponse rejectUser(
    						        Long userId,
    						        String reason
    						) {

    						    User user = userRepository.findById(userId)
    						            .orElseThrow(() ->
    						                    new ResourceNotFoundException(
    						                            MessageConstants.USER_NOT_FOUND
    						                    ));

    						    user.setStatus(UserStatus.REJECTED);

    						    user.setApprovalStage(
    						            ApprovalStage.REJECTED
    						    );

    						    user.setRejectionReason(reason);

    						    userRepository.save(user);

    						    auditLogService.createAuditLog(
    						            user.getId(),
    						            "USER_REJECTED",
    						            "User rejected by Super Admin"
    						    );

    						    return TokenResponse.builder()
    						            .success(true)
    						            .message("User rejected successfully")
    						            .build();
    						}
    						


    		/*Call validateRegisterRequest()
Add login attempt lockout
Add OTP resend rate limiting
Prevent multiple active OTPs
Delete refresh token during logout
Validate status before approve/reject
Enforce SUPER_ADMIN authorization in service layer
Support multiple concurrent sessions properly

Add password history validation*/

}


