package com.lms.usermanagementservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long userId;

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;

    private ProfileResponse user;

    private Boolean success;

    private String message;
}
