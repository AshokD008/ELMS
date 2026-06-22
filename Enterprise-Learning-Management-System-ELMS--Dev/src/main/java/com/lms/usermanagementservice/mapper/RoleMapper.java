package com.lms.usermanagementservice.mapper;

import com.lms.usermanagementservice.dto.request.CreatePermissionRequest;
import com.lms.usermanagementservice.dto.request.CreateRoleRequest;
import com.lms.usermanagementservice.dto.request.UpdatePermissionRequest;
import com.lms.usermanagementservice.dto.request.UpdateRoleRequest;
import com.lms.usermanagementservice.dto.response.PermissionResponse;
import com.lms.usermanagementservice.dto.response.RoleResponse;
import com.lms.usermanagementservice.entity.Permission;
import com.lms.usermanagementservice.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    RoleResponse toRoleResponse(Role role);

    default RoleResponse toResponse(Role role) {
        return toRoleResponse(role);
    }

    PermissionResponse toPermissionResponse(Permission permission);

    default PermissionResponse toResponse(Permission permission) {
        return toPermissionResponse(permission);
    }

    @Mapping(target = "id", ignore = true)
    Role toEntity(CreateRoleRequest request);

    @Mapping(target = "id", ignore = true)
    void updateRoleFromRequest(UpdateRoleRequest request,
                               @MappingTarget Role role);

    @Mapping(target = "id", ignore = true)
    Permission toPermissionEntity(CreatePermissionRequest request);

    @Mapping(target = "id", ignore = true)
    void updatePermissionFromRequest(UpdatePermissionRequest request,
                                     @MappingTarget Permission permission);
}
