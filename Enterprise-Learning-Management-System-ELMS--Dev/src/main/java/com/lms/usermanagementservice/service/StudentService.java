package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.request.CreateStudentRequest;
import com.lms.usermanagementservice.dto.request.UpdateStudentRequest;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.StudentResponse;

import java.util.List;

public interface StudentService {

    StudentResponse createStudent(CreateStudentRequest request);

    StudentResponse updateStudent(
            Long studentId,
            UpdateStudentRequest request
    );

    StudentResponse getStudentById(Long studentId);

    PageResponse<StudentResponse> getAllStudents(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    PageResponse<StudentResponse> searchStudents(
            String keyword,
            int page,
            int size
    );

    List<StudentResponse> getStudentsByCollege(Long collegeId);

    StudentResponse activateStudent(Long studentId);

    StudentResponse deactivateStudent(Long studentId);

    boolean existsByEmail(String email);

    boolean existsByRollNumber(String rollNumber);

    void deleteStudent(Long studentId);

    void softDeleteStudent(Long studentId);

    StudentResponse getCurrentStudentProfile();
}