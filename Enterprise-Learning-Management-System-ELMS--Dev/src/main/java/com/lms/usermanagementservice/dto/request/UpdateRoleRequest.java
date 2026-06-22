package com.lms.usermanagementservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    @Size(max = 100)
    private String roleName;

    @Size(max = 255)
    private String description;

    public String getName() {
        return roleName;
    }
}
