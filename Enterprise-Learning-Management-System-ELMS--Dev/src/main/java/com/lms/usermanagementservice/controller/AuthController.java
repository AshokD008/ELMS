package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.request.ForgotPasswordRequest;
import com.lms.usermanagementservice.dto.request.LoginRequest;
import com.lms.usermanagementservice.dto.request.LogoutRequest;
import com.lms.usermanagementservice.dto.request.RefreshTokenRequest;
import com.lms.usermanagementservice.dto.request.RegisterRequest;
import com.lms.usermanagementservice.dto.request.ResendOTPRequest;
import com.lms.usermanagementservice.dto.request.ResetPasswordRequest;
import com.lms.usermanagementservice.dto.request.VerifyOTPRequest;
import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.AuthResponse;
import com.lms.usermanagementservice.dto.response.LoginResponse;
import com.lms.usermanagementservice.dto.response.RefreshTokenResponse;
import com.lms.usermanagementservice.dto.response.TokenResponse;
import com.lms.usermanagementservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Auth Controller",
        description = "Authentication Management APIs"
)
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register User",
            description = "Register new user account"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Registration successful"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "User registered successfully",
                                response
                        )
                );
    }

    @PostMapping("/register/super-admin")
    public ResponseEntity<ApiResponse<AuthResponse>> registerSuperAdmin(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Super admin registered successfully", authService.registerSuperAdmin(request)));
    }

    @PostMapping("/register/college-admin")
    public ResponseEntity<ApiResponse<AuthResponse>> registerCollegeAdmin(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "College admin registered successfully", authService.registerCollegeAdmin(request)));
    }

    @PostMapping("/register/faculty")
    public ResponseEntity<ApiResponse<AuthResponse>> registerFaculty(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Faculty registered successfully", authService.registerFaculty(request)));
    }

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<AuthResponse>> registerStudent(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Student registered successfully", authService.registerStudent(request)));
    }

    @Operation(
            summary = "User Login",
            description = "Authenticate user and generate JWT tokens"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Account blocked or inactive",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        response
                )
        );
    }

    @Operation(
            summary = "Refresh Access Token",
            description = "Generate new access token using refresh token"
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        RefreshTokenResponse response =
                authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Token refreshed successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Verify OTP",
            description = "Verify email verification OTP"
    )
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyOtp(
            @Valid @RequestBody VerifyOTPRequest request
    ) {

        TokenResponse response =
                authService.verifyOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "OTP verified successfully",
                        response
                )
        );
    }

    @PostMapping("/verify-login-otp")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyLoginOtp(
            @Valid @RequestBody VerifyOTPRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Login OTP verified successfully", authService.verifyLoginOtp(request)));
    }

    @Operation(
            summary = "Resend OTP",
            description = "Resend verification OTP"
    )
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<TokenResponse>> resendOtp(
            @Valid @RequestBody ResendOTPRequest request
    ) {

        TokenResponse response =
                authService.resendOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "OTP resent successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Forgot Password",
            description = "Send password reset OTP"
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<TokenResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        TokenResponse response =
                authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password reset OTP sent successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Reset Password",
            description = "Reset user password using OTP"
    )
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<TokenResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        TokenResponse response =
                authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password reset successful",
                        response
                )
        );
    }

    @Operation(
            summary = "Logout User",
            description = "Logout current user session"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TokenResponse>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {

        TokenResponse response =
                authService.logout(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logout successful",
                        response
                )
        );
    }

    @Operation(
            summary = "Logout From All Devices",
            description = "Logout user from all active devices"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout-all-devices/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TokenResponse>> logoutAllDevices(
            @PathVariable Long userId
    ) {

        TokenResponse response =
                authService.logoutAllDevices(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logged out from all devices successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Current User Profile",
            description = "Fetch currently authenticated user profile"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUserProfile() {

        AuthResponse response =
                authService.getCurrentUserProfile();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current user profile fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Validate Access Token",
            description = "Validate JWT access token"
    )
    @PostMapping("/validate-access-token")
    public ResponseEntity<ApiResponse<Boolean>> validateAccessToken(
            @RequestParam String token
    ) {

        Boolean response =
                authService.validateAccessToken(token);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Access token validation completed",
                        response
                )
        );
    }

    @Operation(
            summary = "Validate Refresh Token",
            description = "Validate refresh token"
    )
    @PostMapping("/validate-refresh-token")
    public ResponseEntity<ApiResponse<Boolean>> validateRefreshToken(
            @RequestParam String refreshToken
    ) {

        Boolean response =
                authService.validateRefreshToken(refreshToken);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Refresh token validation completed",
                        response
                )
        );
    }
}
