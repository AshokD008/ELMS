package com.lms.usermanagementservice.dto.request;

import com.lms.usermanagementservice.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionRequest {

    private String permissionName;

    private PermissionType permissionType;

    private String description;

    public String getName() {
        return permissionName;
    }
}
