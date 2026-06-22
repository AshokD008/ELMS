package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.response.AuthResponse;

public interface OAuthService {

    AuthResponse googleLogin(
            String authorizationCode
    );

    AuthResponse githubLogin(
            String authorizationCode
    );

    AuthResponse microsoftLogin(
            String authorizationCode
    );

    AuthResponse handleOAuthLogin(
            String provider,
            String authorizationCode
    );

    AuthResponse linkOAuthAccount(
            Long userId,
            String provider,
            String providerUserId
    );

    AuthResponse unlinkOAuthAccount(
            Long userId,
            String provider
    );

    Boolean isOAuthAccountLinked(
            Long userId,
            String provider
    );

    void revokeOAuthAccess(
            Long userId,
            String provider
    );
}