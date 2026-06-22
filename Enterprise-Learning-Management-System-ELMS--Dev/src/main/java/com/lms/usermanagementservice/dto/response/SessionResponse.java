package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.SessionStatus;
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
public class SessionResponse {

    private UUID id;

    private String deviceName;

    private String ipAddress;

    private String userAgent;

    private SessionStatus status;

    private LocalDateTime loginAt;

    private LocalDateTime logoutAt;

    private LocalDateTime expiresAt;
}