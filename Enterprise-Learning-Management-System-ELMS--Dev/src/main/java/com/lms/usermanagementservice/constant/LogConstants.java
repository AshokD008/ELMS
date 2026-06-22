
package com.lms.usermanagementservice.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LogConstants {

    public static final String USER_LOGIN =
            "User logged in successfully";

    public static final String USER_LOGOUT =
            "User logged out successfully";

    public static final String USER_REGISTERED =
            "New user registered";

    public static final String PASSWORD_RESET =
            "Password reset operation completed";

    public static final String OTP_GENERATED =
            "OTP generated successfully";

    public static final String OTP_VERIFIED =
            "OTP verified successfully";

    public static final String ROLE_ASSIGNED =
            "Role assigned to user";

    public static final String PERMISSION_ASSIGNED =
            "Permission assigned to role";

    public static final String ACCESS_DENIED =
            "Unauthorized access attempt detected";

    public static final String INVALID_TOKEN =
            "Invalid JWT token detected";

    public static final String EXCEPTION_OCCURRED =
            "Unexpected exception occurred";

    public static final String DATABASE_OPERATION_SUCCESS =
            "Database operation completed successfully";

    public static final String DATABASE_OPERATION_FAILED =
            "Database operation failed";

    public static final String AUTH_REGISTER_INITIATED = "User registration initiated";
    public static final String AUTH_REGISTER_SUCCESS = "User registration completed";
    public static final String AUTH_LOGIN_INITIATED = "User login initiated";
    public static final String AUTH_LOGIN_SUCCESS = "User login completed";
    public static final String COLLEGE_CREATE_INITIATED = "College creation initiated";
    public static final String COLLEGE_CREATE_SUCCESS = "College creation completed";
    public static final String COLLEGE_ADMIN_CREATE_INITIATED = "College admin creation initiated";
    public static final String COLLEGE_ADMIN_CREATE_SUCCESS = "College admin creation completed";
    public static final String STUDENT_CREATE_INITIATED = "Student creation initiated";
    public static final String STUDENT_CREATE_SUCCESS = "Student creation completed";
    public static final String SUPER_ADMIN_CREATE_INITIATED = "Super admin creation initiated";
    public static final String SUPER_ADMIN_CREATE_SUCCESS = "Super admin creation completed";
    public static final String ROLE_CREATE_INITIATED = "Role creation initiated";
    public static final String ROLE_CREATE_SUCCESS = "Role creation completed";
    public static final String PERMISSION_CREATE_INITIATED = "Permission creation initiated";
    public static final String PERMISSION_CREATE_SUCCESS = "Permission creation completed";
    public static final String PROFILE_UPDATE_INITIATED = "Profile update initiated";
    public static final String PROFILE_UPDATE_SUCCESS = "Profile update completed";
    public static final String DEVICE_REGISTER_INITIATED = "Device registration initiated";
    public static final String DEVICE_REGISTER_SUCCESS = "Device registration completed";
    public static final String SESSION_CREATE_INITIATED = "Session creation initiated";
    public static final String SESSION_CREATE_SUCCESS = "Session creation completed";
    public static final String VERIFICATION_CREATE_INITIATED = "Verification creation initiated";
    public static final String VERIFICATION_CREATE_SUCCESS = "Verification creation completed";
    public static final String OAUTH_LOGIN_INITIATED = "OAuth login initiated";
    public static final String OAUTH_LOGIN_SUCCESS = "OAuth login completed";
    public static final String OTP_GENERATION_STARTED = "OTP generation started";
    public static final String OTP_GENERATION_COMPLETED = "OTP generation completed";
    public static final String JWT_ACCESS_TOKEN_GENERATED = "JWT access token generated";
    public static final String JWT_REFRESH_TOKEN_GENERATED = "JWT refresh token generated";
    public static final String JWT_TOKEN_BLACKLISTED = "JWT token blacklisted";
    public static final String JWT_ALL_TOKENS_INVALIDATED = "All JWT tokens invalidated";
    public static final String REFRESH_TOKEN_CREATED = "Refresh token created";
    public static final String AUDIT_LOG_CREATED = "Audit log created";
}
