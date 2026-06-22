package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.request.ForgotPasswordRequest;
import com.lms.usermanagementservice.dto.request.LoginRequest;
import com.lms.usermanagementservice.dto.request.LogoutRequest;
import com.lms.usermanagementservice.dto.request.RefreshTokenRequest;
import com.lms.usermanagementservice.dto.request.RegisterRequest;
import com.lms.usermanagementservice.dto.request.ResetPasswordRequest;
import com.lms.usermanagementservice.dto.request.ResendOTPRequest;
import com.lms.usermanagementservice.dto.request.VerifyOTPRequest;
import com.lms.usermanagementservice.dto.response.AuthResponse;
import com.lms.usermanagementservice.dto.response.LoginResponse;
import com.lms.usermanagementservice.dto.response.RefreshTokenResponse;
import com.lms.usermanagementservice.dto.response.TokenResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse registerSuperAdmin(RegisterRequest request);
    AuthResponse registerCollegeAdmin(RegisterRequest request);
    AuthResponse registerFaculty(RegisterRequest request);
    AuthResponse registerStudent(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    TokenResponse verifyOtp(VerifyOTPRequest request);

    TokenResponse verifyLoginOtp(VerifyOTPRequest request);

    TokenResponse resendOtp(ResendOTPRequest request);

    TokenResponse forgotPassword(ForgotPasswordRequest request);

    TokenResponse resetPassword(ResetPasswordRequest request);

    TokenResponse logout(LogoutRequest request);

    TokenResponse logoutAllDevices(Long userId);

    AuthResponse getCurrentUserProfile();

    Boolean validateAccessToken(String token);

    Boolean validateRefreshToken(String refreshToken);
 
    		TokenResponse approveUser(Long userId);

    		TokenResponse rejectUser(Long userId, String reason);
    		

}
