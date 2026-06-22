package com.lms.usermanagementservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "OTP is required")
    private String otpCode;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 20)
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    public String getOtp() {
        return otpCode;
    }
}
