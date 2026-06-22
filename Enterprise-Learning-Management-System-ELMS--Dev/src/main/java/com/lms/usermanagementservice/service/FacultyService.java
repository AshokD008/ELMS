package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.response.FacultyResponse;
import java.util.List;

public interface FacultyService {
    FacultyResponse getFaculty(Long facultyId);
    List<FacultyResponse> getScopedFaculty();
}
