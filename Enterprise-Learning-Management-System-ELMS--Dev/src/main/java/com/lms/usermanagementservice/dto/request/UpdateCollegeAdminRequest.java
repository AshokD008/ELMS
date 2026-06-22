package com.lms.usermanagementservice.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCollegeAdminRequest {

    private String firstName;

    private String lastName;

    @Pattern(regexp = "^[0-9]{10}$")
    private String phoneNumber;

    private String profileImageUrl;

    private String designation;

    private String employeeId;
}
