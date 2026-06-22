package com.lms.usermanagementservice.dto.request;

import com.lms.usermanagementservice.enums.OTPType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendOTPRequest {

    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "OTP type is required")
    private OTPType otpType;
}