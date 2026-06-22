package com.lms.usermanagementservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollegeRequest {

    @NotBlank(message = "College name is required")
    private String collegeName;

    private String collegeCode;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String address;
}
