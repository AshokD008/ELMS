package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.AuthResponse;
import com.lms.usermanagementservice.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
@Tag(
        name = "OAuth Controller",
        description = "OAuth Authentication Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
@Validated
public class OAuthController {

    private final OAuthService oauthService;

    @Operation(
            summary = "Google OAuth Login",
            description = "Authenticate user using Google OAuth"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Google login successful"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid authorization code",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(
            @RequestParam
            @NotBlank(message = "Authorization code is required")
            String authorizationCode
    ) {

        AuthResponse response =
                oauthService.googleLogin(
                        authorizationCode
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Google OAuth login successful",
                        response
                )
        );
    }

    @Operation(
            summary = "GitHub OAuth Login",
            description = "Authenticate user using GitHub OAuth"
    )
    @PostMapping("/github")
    public ResponseEntity<ApiResponse<AuthResponse>> githubLogin(
            @RequestParam
            @NotBlank(message = "Authorization code is required")
            String authorizationCode
    ) {

        AuthResponse response =
                oauthService.githubLogin(
                        authorizationCode
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "GitHub OAuth login successful",
                        response
                )
        );
    }

    @Operation(
            summary = "Microsoft OAuth Login",
            description = "Authenticate user using Microsoft OAuth"
    )
    @PostMapping("/microsoft")
    public ResponseEntity<ApiResponse<AuthResponse>> microsoftLogin(
            @RequestParam
            @NotBlank(message = "Authorization code is required")
            String authorizationCode
    ) {

        AuthResponse response =
                oauthService.microsoftLogin(
                        authorizationCode
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Microsoft OAuth login successful",
                        response
                )
        );
    }

    @Operation(
            summary = "Generic OAuth Login",
            description = "Authenticate user using provider name"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> handleOAuthLogin(
            @RequestParam
            @NotBlank(message = "Provider is required")
            String provider,

            @RequestParam
            @NotBlank(message = "Authorization code is required")
            String authorizationCode
    ) {

        AuthResponse response =
                oauthService.handleOAuthLogin(
                        provider,
                        authorizationCode
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "OAuth login successful",
                        response
                )
        );
    }

    @Operation(
            summary = "Link OAuth Account",
            description = "Link OAuth provider account to user"
    )
    @PostMapping("/link")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AuthResponse>> linkOAuthAccount(
            @RequestParam @Positive Long userId,
            @RequestParam @NotBlank String provider,
            @RequestParam @NotBlank String providerUserId
    ) {

        AuthResponse response =
                oauthService.linkOAuthAccount(
                        userId,
                        provider,
                        providerUserId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "OAuth account linked successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Unlink OAuth Account",
            description = "Unlink OAuth provider account"
    )
    @DeleteMapping("/unlink")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AuthResponse>> unlinkOAuthAccount(
            @RequestParam @Positive Long userId,
            @RequestParam @NotBlank String provider
    ) {

        AuthResponse response =
                oauthService.unlinkOAuthAccount(
                        userId,
                        provider
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "OAuth account unlinked successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Check OAuth Account Linked",
            description = "Check whether OAuth provider is linked"
    )
    @GetMapping("/linked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> isOAuthAccountLinked(
            @RequestParam @Positive Long userId,
            @RequestParam @NotBlank String provider
    ) {

        Boolean response =
                oauthService.isOAuthAccountLinked(
                        userId,
                        provider
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "OAuth account validation completed",
                        response
                )
        );
    }

    @Operation(
            summary = "Revoke OAuth Access",
            description = "Revoke OAuth provider access"
    )
    @DeleteMapping("/revoke")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeOAuthAccess(
            @RequestParam Long userId,
            @RequestParam String provider
    ) {

        oauthService.revokeOAuthAccess(
                userId,
                provider
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "OAuth access revoked successfully"
                )
        );
    }
}
