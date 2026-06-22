package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private Long permissionId;

    private String permissionName;

    private PermissionType permissionType;

    private String description;
}