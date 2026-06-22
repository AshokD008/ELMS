package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.OTPStatus;
import com.lms.usermanagementservice.enums.OTPType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPResponse {

    private Long otpId;

    private String email;

    private OTPType otpType;

    private OTPStatus otpStatus;

    private LocalDateTime expiresAt;
}