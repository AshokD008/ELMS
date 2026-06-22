package com.lms.usermanagementservice.dto.request;

import com.lms.usermanagementservice.enums.Gender;
import com.lms.usermanagementservice.enums.UserRoleType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20)
    private String password;

    @NotNull(message = "Gender is required")
    private Gender gender;
    
    private UserRoleType role;

    private Long collegeId;

    private String collegeAdminId;

    private String designation;

    private String department;

    private String studentRollNumber;

    private String admissionNumber;

    private String year;

    private String semester;

    private String section;
}
