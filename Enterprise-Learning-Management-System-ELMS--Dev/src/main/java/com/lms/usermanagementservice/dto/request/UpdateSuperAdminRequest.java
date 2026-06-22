package com.lms.usermanagementservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSuperAdminRequest {

    private String firstName;

    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid mobile number"
    )
    private String phoneNumber;

    private String department;

    private String accessLevel;
}
