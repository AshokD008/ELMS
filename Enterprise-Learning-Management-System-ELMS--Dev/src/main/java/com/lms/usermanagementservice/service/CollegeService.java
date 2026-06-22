package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.request.CreateCollegeRequest;
import com.lms.usermanagementservice.dto.request.UpdateCollegeRequest;
import com.lms.usermanagementservice.dto.response.CollegeResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;

import java.util.List;

public interface CollegeService {

    CollegeResponse createCollege(
            CreateCollegeRequest request
    );

    CollegeResponse updateCollege(
            Long collegeId,
            UpdateCollegeRequest request
    );

    CollegeResponse getCollegeById(Long collegeId);

    CollegeResponse getCollegeByCode(String code);

    PageResponse<CollegeResponse> getAllColleges(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    PageResponse<CollegeResponse> searchColleges(
            String keyword,
            int page,
            int size
    );

    List<CollegeResponse> getActiveColleges();

    CollegeResponse activateCollege(Long collegeId);

    CollegeResponse deactivateCollege(Long collegeId);

    boolean existsByCollegeCode(String collegeCode);

    boolean existsByEmail(String email);
    void deleteCollege(Long collegeId);

    void softDeleteCollege(Long collegeId);
}