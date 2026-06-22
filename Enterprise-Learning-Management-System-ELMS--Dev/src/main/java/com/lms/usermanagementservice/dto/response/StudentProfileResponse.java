package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponse {

    private Long studentProfileId;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private Gender gender;

    private LocalDate dateOfBirth;

    private String profileImageUrl;

    private String studentRollNumber;

    private CollegeResponse college;
}