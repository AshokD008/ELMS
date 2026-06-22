package com.lms.usermanagementservice.mapper;

import com.lms.usermanagementservice.dto.request.CreateCollegeRequest;
import com.lms.usermanagementservice.dto.request.UpdateCollegeRequest;
import com.lms.usermanagementservice.dto.response.CollegeResponse;
import com.lms.usermanagementservice.entity.College;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CollegeMapper {

    CollegeResponse toCollegeResponse(College college);

    default CollegeResponse toResponse(College college) {
        return toCollegeResponse(college);
    }

    @Mapping(target = "id", ignore = true)
    College toEntity(CreateCollegeRequest request);

    @Mapping(target = "id", ignore = true)
    void updateCollegeFromRequest(UpdateCollegeRequest request,
                                  @MappingTarget College college);
}
