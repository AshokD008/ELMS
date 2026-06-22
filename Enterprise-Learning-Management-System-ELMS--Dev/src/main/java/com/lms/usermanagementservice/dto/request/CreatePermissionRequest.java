package com.lms.usermanagementservice.dto.request;

import com.lms.usermanagementservice.enums.PermissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {

    @NotBlank(message = "Permission name is required")
    private String permissionName;

    @NotNull(message = "Permission type is required")
    private PermissionType permissionType;

    private String description;

    public String getName() {
        return permissionName;
    }
}
