package com.lms.usermanagementservice.dto.request;

import com.lms.usermanagementservice.enums.Gender;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Pattern(regexp = "^[0-9]{10}$")
    private String phoneNumber;

    private Gender gender;

    private LocalDate dateOfBirth;

    private Long collegeId;

    private String profileImageUrl;

    private String department;

    private String year;

    private String semester;

    private String section;
}
