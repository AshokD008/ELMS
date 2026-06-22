package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.response.AuthResponse;
import com.lms.usermanagementservice.entity.OAuthAccount;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.AuthProvider;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.enums.UserStatus;
import com.lms.usermanagementservice.exception.DuplicateResourceException;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.mapper.UserMapper;
import com.lms.usermanagementservice.repository.OAuthAccountRepository;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.JwtService;
import com.lms.usermanagementservice.service.OAuthService;
import com.lms.usermanagementservice.service.OTPService;
import com.lms.usermanagementservice.enums.OTPType;
import com.lms.usermanagementservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private static final String OAUTH_CACHE_PREFIX =
            "OAUTH:";

    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;

    private final UserMapper userMapper;

    private final JwtService jwtService;

    private final SecurityUtil securityUtil;

    private final PasswordEncoder passwordEncoder;

    private final AuditLogService auditLogService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final OTPService otpService;

    @Override
    @Transactional
    public AuthResponse googleLogin(
            String authorizationCode
    ) {

        return handleOAuthLogin(
                AuthProvider.GOOGLE.name(),
                authorizationCode
        );
    }

    @Override
    @Transactional
    public AuthResponse githubLogin(
            String authorizationCode
    ) {

        return handleOAuthLogin(
                AuthProvider.GITHUB.name(),
                authorizationCode
        );
    }

    @Override
    @Transactional
    public AuthResponse microsoftLogin(
            String authorizationCode
    ) {

        return handleOAuthLogin(
                AuthProvider.MICROSOFT.name(),
                authorizationCode
        );
    }

    @Override
    @Transactional
    public AuthResponse handleOAuthLogin(
            String provider,
            String authorizationCode
    ) {

        log.info(LogConstants.OAUTH_LOGIN_INITIATED);

        validateOAuthRequest(
                provider,
                authorizationCode
        );

        AuthProvider authProvider =
                AuthProvider.valueOf(
                        provider.toUpperCase()
                );

        /*
         * Real production integration:
         * Exchange authorization code
         * with OAuth provider APIs.
         */

        String providerUserId =
                UUID.randomUUID().toString();

        String email =
                provider.toLowerCase()
                        + "_user@lmstelugu.com";

        String fullName =
                provider + " User";

        OAuthAccount existingOAuth =
                oauthAccountRepository
                        .findByProviderAndProviderUserId(
                                authProvider,
                                providerUserId
                        )
                        .orElse(null);

        User user;

        if (existingOAuth != null) {

            user = existingOAuth.getUser();

        } else {

            user = userRepository.findByEmail(email)
                    .orElseGet(() ->
                            createOAuthUser(
                                    email,
                                    fullName,
                                    authProvider
                            )
                    );

            OAuthAccount oauthAccount =
                    new OAuthAccount();

            oauthAccount.setUser(user);
            oauthAccount.setProvider(authProvider);
            oauthAccount.setProviderUserId(
                    providerUserId
            );
            oauthAccount.setEmail(email);
            oauthAccount.setLinkedAt(
                    LocalDateTime.now()
            );

            oauthAccountRepository.save(oauthAccount);

            cacheOAuthAccount(oauthAccount);
        }

        if (Boolean.TRUE.equals(user.getAccountLocked())) {

            throw new ForbiddenException(
                    MessageConstants.ACCOUNT_BLOCKED
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE
                || !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ForbiddenException("OAuth account is pending required approval");
        }

        otpService.generateAndSendOtp(user, OTPType.LOGIN_VERIFICATION);

        auditLogService.createAuditLog(
                user.getId(),
                "OAUTH_LOGIN",
                provider + " OAuth login successful"
        );

        log.info(LogConstants.OAUTH_LOGIN_SUCCESS);

        return AuthResponse.builder()
                .success(true)
                .message(
                        "Login OTP sent to your verified email"
                )
                .user(
                        userMapper.toProfileResponse(user)
                )
                .build();
    }

    @Override
    @Transactional
    public AuthResponse linkOAuthAccount(
            Long userId,
            String provider,
            String providerUserId
    ) {

        User currentUser =
                securityUtil.getCurrentUser();

        validateOwnership(
                currentUser,
                userId
        );

        AuthProvider authProvider =
                AuthProvider.valueOf(
                        provider.toUpperCase()
                );

        boolean alreadyLinked =
                oauthAccountRepository
                        .existsByProviderAndProviderUserId(
                                authProvider,
                                providerUserId
                        );

        if (alreadyLinked) {

            throw new DuplicateResourceException(
                    MessageConstants.OAUTH_ACCOUNT_ALREADY_LINKED
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                MessageConstants.USER_NOT_FOUND
                        ));

        OAuthAccount oauthAccount =
                new OAuthAccount();

        oauthAccount.setUser(user);
        oauthAccount.setProvider(authProvider);
        oauthAccount.setProviderUserId(
                providerUserId
        );
        oauthAccount.setEmail(user.getEmail());
        oauthAccount.setLinkedAt(
                LocalDateTime.now()
        );

        OAuthAccount savedAccount =
                oauthAccountRepository.save(
                        oauthAccount
                );

        cacheOAuthAccount(savedAccount);

        auditLogService.createAuditLog(
                userId,
                "OAUTH_ACCOUNT_LINKED",
                provider + " account linked"
        );

        return AuthResponse.builder()
                .success(true)
                .message(
                        MessageConstants.OAUTH_ACCOUNT_LINKED
                )
                .build();
    }

    @Override
    @Transactional
    public AuthResponse unlinkOAuthAccount(
            Long userId,
            String provider
    ) {

        User currentUser =
                securityUtil.getCurrentUser();

        validateOwnership(
                currentUser,
                userId
        );

        AuthProvider authProvider =
                AuthProvider.valueOf(
                        provider.toUpperCase()
                );

        OAuthAccount oauthAccount =
                oauthAccountRepository
                        .findByUserIdAndProvider(
                                userId,
                                authProvider
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.OAUTH_ACCOUNT_NOT_FOUND
                                ));

        oauthAccountRepository.delete(oauthAccount);

        redisTemplate.delete(
                OAUTH_CACHE_PREFIX
                        + oauthAccount.getId()
        );

        auditLogService.createAuditLog(
                userId,
                "OAUTH_ACCOUNT_UNLINKED",
                provider + " account unlinked"
        );

        return AuthResponse.builder()
                .success(true)
                .message(
                        MessageConstants.OAUTH_ACCOUNT_UNLINKED
                )
                .build();
    }

    @Override
    public Boolean isOAuthAccountLinked(
            Long userId,
            String provider
    ) {

        validateOwnership(securityUtil.getCurrentUser(), userId);

        AuthProvider authProvider =
                AuthProvider.valueOf(
                        provider.toUpperCase()
                );

        return oauthAccountRepository
                .existsByUserIdAndProvider(
                        userId,
                        authProvider
                );
    }

    @Override
    @Transactional
    public void revokeOAuthAccess(
            Long userId,
            String provider
    ) {

        validateAdminAccess();

        AuthProvider authProvider =
                AuthProvider.valueOf(
                        provider.toUpperCase()
                );

        OAuthAccount oauthAccount =
                oauthAccountRepository
                        .findByUserIdAndProvider(
                                userId,
                                authProvider
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        MessageConstants.OAUTH_ACCOUNT_NOT_FOUND
                                ));

        oauthAccountRepository.delete(oauthAccount);

        redisTemplate.delete(
                OAUTH_CACHE_PREFIX
                        + oauthAccount.getId()
        );

        auditLogService.createAuditLog(
                securityUtil.getCurrentUser().getId(),
                "OAUTH_ACCESS_REVOKED",
                provider + " OAuth access revoked"
        );
    }

    private User createOAuthUser(
            String email,
            String fullName,
            AuthProvider provider
    ) {

        User user = new User();

        String[] names =
                fullName.split(" ");

        user.setFirstName(names[0]);

        if (names.length > 1) {
            user.setLastName(names[1]);
        }

        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(
                        UUID.randomUUID().toString()
                )
        );
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setIsDeleted(false);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setProvider(provider);

        return userRepository.save(user);
    }

    private void validateOAuthRequest(
            String provider,
            String authorizationCode
    ) {

        if (!StringUtils.hasText(provider)) {

            throw new ValidationException(
                    MessageConstants.PROVIDER_REQUIRED
            );
        }

        if (!StringUtils.hasText(
                authorizationCode
        )) {

            throw new ValidationException(
                    MessageConstants.AUTHORIZATION_CODE_REQUIRED
            );
        }
    }

    private void validateOwnership(
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

    private void cacheOAuthAccount(
            OAuthAccount oauthAccount
    ) {

        redisTemplate.opsForValue().set(
                OAUTH_CACHE_PREFIX
                        + oauthAccount.getId(),
                oauthAccount,
                Duration.ofHours(12)
        );
    }
}
