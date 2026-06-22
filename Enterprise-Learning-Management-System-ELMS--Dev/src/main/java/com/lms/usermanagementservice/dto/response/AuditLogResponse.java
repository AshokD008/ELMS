package com.lms.usermanagementservice.dto.response;

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
public class AuditLogResponse {

    private UUID id;

    private String action;

    private String module;

    private String performedBy;

    private String ipAddress;

    private String deviceInfo;

    private LocalDateTime createdAt;
}