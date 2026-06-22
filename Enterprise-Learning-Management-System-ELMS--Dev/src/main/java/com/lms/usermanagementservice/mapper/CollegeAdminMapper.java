package com.lms.usermanagementservice.mapper;

import com.lms.usermanagementservice.dto.response.CollegeAdminResponse;
import com.lms.usermanagementservice.entity.CollegeAdminProfile;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CollegeAdminMapper {

    CollegeAdminResponse toResponse(CollegeAdminProfile entity);
}