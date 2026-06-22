package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.ProfileResponse;
import com.lms.usermanagementservice.dto.request.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileResponse getCurrentProfile();

    ProfileResponse getProfileByUserId(Long userId);

    ProfileResponse updateProfile(
            UpdateProfileRequest request
    );

    ProfileResponse uploadProfilePicture(
            MultipartFile file
    );

    void removeProfilePicture();

    ProfileResponse updateEmail(
            Long userId,
            String email
    );

    ProfileResponse updatePhoneNumber(
            Long userId,
            String phoneNumber
    );

    ProfileResponse activateProfile(Long userId);

    ProfileResponse deactivateProfile(Long userId);

    PageResponse<ProfileResponse> searchProfiles(
            String keyword,
            int page,
            int size
    );
}