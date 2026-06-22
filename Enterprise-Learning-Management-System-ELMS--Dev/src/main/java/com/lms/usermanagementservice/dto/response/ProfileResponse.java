package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.Gender;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long id;

    private Long userId;

    private Boolean emailVerified;

    private Boolean phoneVerified;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private Gender gender;

    private LocalDate dateOfBirth;

    private String profileImageUrl;

    private String profileImage;

    private UserStatus userStatus;

    private UserStatus status;

    private List<UserRoleType> roles;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
