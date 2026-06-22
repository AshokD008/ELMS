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
public class CollegeAdminResponse {

    private Long id;

    private Long collegeAdminId;

    private Long userId;

    private ProfileResponse user;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String profileImageUrl;

    private UserStatus userStatus;

    private CollegeResponse college;

    private Long collegeId;

    private String collegeName;

    private String designation;

    private String employeeId;

    private LocalDateTime joiningDate;
}
