package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {

    private UUID id;

    private String verificationType;

    private VerificationStatus status;

    private Boolean verified;

    private LocalDateTime verifiedAt;

    private LocalDateTime createdAt;
}