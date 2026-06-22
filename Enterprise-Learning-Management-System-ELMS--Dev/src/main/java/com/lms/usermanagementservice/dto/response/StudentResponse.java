package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.Gender;
import com.lms.usermanagementservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long studentId;

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private Gender gender;

    private LocalDate dateOfBirth;

    private String studentRollNumber;

    private UserStatus userStatus;

    private CollegeResponse college;
}