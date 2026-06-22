package com.lms.usermanagementservice.mapper;

import com.lms.usermanagementservice.dto.request.CreateStudentRequest;
import com.lms.usermanagementservice.dto.request.UpdateStudentRequest;
import com.lms.usermanagementservice.dto.response.StudentProfileResponse;
import com.lms.usermanagementservice.dto.response.StudentResponse;
import com.lms.usermanagementservice.entity.StudentProfile;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentMapper {

    StudentResponse toStudentResponse(StudentProfile studentProfile);

    default StudentResponse toResponse(StudentProfile studentProfile) {
        return toStudentResponse(studentProfile);
    }

    StudentProfileResponse toStudentProfileResponse(StudentProfile studentProfile);

    @Mapping(target = "id", ignore = true)
    StudentProfile toEntity(CreateStudentRequest request);

    @Mapping(target = "id", ignore = true)
    void updateStudentFromRequest(UpdateStudentRequest request,
                                  @MappingTarget StudentProfile studentProfile);
}
