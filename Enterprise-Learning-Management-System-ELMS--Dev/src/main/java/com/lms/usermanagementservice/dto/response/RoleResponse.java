package com.lms.usermanagementservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Long roleId;

    private String roleName;

    private String description;

    private List<PermissionResponse> permissions;
}