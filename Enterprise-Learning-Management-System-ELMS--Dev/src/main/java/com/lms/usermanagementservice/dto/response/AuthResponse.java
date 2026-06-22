package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private UserStatus userStatus;

    private List<UserRoleType> roles;

    private TokenResponse token;

    private Boolean success;

    private String message;

    private ProfileResponse data;

    private ProfileResponse user;

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;
}
