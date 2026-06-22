package com.lms.usermanagementservice.dto.request;

import com.lms.usermanagementservice.enums.OTPType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOTPRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    private String otpCode;

    @NotNull(message = "OTP type is required")
    private OTPType otpType;

    public String getOtp() {
        return otpCode;
    }
}
