package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.FacultyResponse;
import com.lms.usermanagementservice.service.FacultyService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faculty")
@RequiredArgsConstructor
@Validated
public class FacultyController {
    private final FacultyService facultyService;

    @GetMapping("/{facultyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN','FACULTY')")
    public ResponseEntity<ApiResponse<FacultyResponse>> getFaculty(@PathVariable @Positive Long facultyId) {
        return ResponseEntity.ok(ApiResponse.success("Faculty fetched successfully", facultyService.getFaculty(facultyId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<List<FacultyResponse>>> getScopedFaculty() {
        return ResponseEntity.ok(ApiResponse.success("Faculty fetched successfully", facultyService.getScopedFaculty()));
    }
}
