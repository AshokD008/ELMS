package com.lms.usermanagementservice.mapper;

import com.lms.usermanagementservice.dto.request.RegisterRequest;
import com.lms.usermanagementservice.dto.request.UpdateProfileRequest;
import com.lms.usermanagementservice.dto.response.AuthResponse;
import com.lms.usermanagementservice.dto.response.LoginResponse;
import com.lms.usermanagementservice.dto.response.ProfileResponse;
import com.lms.usermanagementservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    AuthResponse toAuthResponse(User user);

    ProfileResponse toProfileResponse(User user);

    LoginResponse toLoginResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(source = "phoneNumber", target = "mobileNumber")
    User toEntity(RegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserFromRequest(UpdateProfileRequest request,
                               @MappingTarget User user);
}