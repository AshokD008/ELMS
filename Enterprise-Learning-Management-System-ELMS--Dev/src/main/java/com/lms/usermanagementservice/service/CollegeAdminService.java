
package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.request.CreateCollegeAdminRequest;
import com.lms.usermanagementservice.dto.request.UpdateCollegeAdminRequest;
import com.lms.usermanagementservice.dto.response.CollegeAdminResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.ProfileResponse;
import com.lms.usermanagementservice.dto.response.TokenResponse;

import java.util.List;

public interface CollegeAdminService {

    CollegeAdminResponse createCollegeAdmin(
            CreateCollegeAdminRequest request
    );

    CollegeAdminResponse updateCollegeAdmin(
            Long adminId,
            UpdateCollegeAdminRequest request
    );

    CollegeAdminResponse getCollegeAdminById(
            Long adminId
    );

    PageResponse<CollegeAdminResponse> getAllCollegeAdmins(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    PageResponse<CollegeAdminResponse> searchCollegeAdmins(
            String keyword,
            int page,
            int size
    );

    List<CollegeAdminResponse> getCollegeAdminsByCollege(
            Long collegeId
    );

    CollegeAdminResponse activateCollegeAdmin(
            Long adminId
    );

    CollegeAdminResponse deactivateCollegeAdmin(
            Long adminId
    );

    void deleteCollegeAdmin(
            Long adminId
    );

    void softDeleteCollegeAdmin(
            Long adminId
    );

    boolean existsByEmail(
            String email
    );

    CollegeAdminResponse getCurrentCollegeAdminProfile();

    PageResponse<ProfileResponse> getFacultyAndStudentApprovals(Long adminId, int page, int size);

    TokenResponse approveCollegeUser(Long adminId, Long userId);
}

