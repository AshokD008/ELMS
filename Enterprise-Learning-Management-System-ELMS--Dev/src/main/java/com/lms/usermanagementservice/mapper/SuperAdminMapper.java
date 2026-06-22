package com.lms.usermanagementservice.mapper;

import com.lms.usermanagementservice.dto.response.SuperAdminResponse;
import com.lms.usermanagementservice.entity.SuperAdminProfile;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SuperAdminMapper {

    SuperAdminResponse toResponse(SuperAdminProfile entity);
}