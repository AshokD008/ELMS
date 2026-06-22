package com.lms.usermanagementservice.mapper;

import com.lms.usermanagementservice.dto.request.CreatePermissionRequest;
import com.lms.usermanagementservice.dto.request.UpdatePermissionRequest;
import com.lms.usermanagementservice.dto.response.PermissionResponse;
import com.lms.usermanagementservice.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

    PermissionResponse toPermissionResponse(Permission permission);

    default PermissionResponse toResponse(Permission permission) {
        return toPermissionResponse(permission);
    }

    @Mapping(target = "id", ignore = true)
    Permission toEntity(CreatePermissionRequest request);

    @Mapping(target = "id", ignore = true)
    void updatePermissionFromRequest(UpdatePermissionRequest request,
                                     @MappingTarget Permission permission);
}
