package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminResponse {

    private Long id;

    private ProfileResponse user;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private UserStatus status;

    private Boolean active;

    private String department;

    private String accessLevel;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;
}
