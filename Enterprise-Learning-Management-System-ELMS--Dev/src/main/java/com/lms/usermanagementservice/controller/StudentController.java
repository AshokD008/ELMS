package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.request.CreateStudentRequest;
import com.lms.usermanagementservice.dto.request.UpdateStudentRequest;
import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.StudentResponse;
import com.lms.usermanagementservice.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(
        name = "Student Controller",
        description = "Student Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    @Operation(
            summary = "Create Student",
            description = "Create new student profile"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Student created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Student already exists",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody CreateStudentRequest request
    ) {

        StudentResponse response =
                studentService.createStudent(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Student created successfully",
                                response
                        )
                );
    }

    @Operation(
            summary = "Update Student",
            description = "Update existing student details"
    )
    @PutMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN','STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable Long studentId,
            @Valid @RequestBody UpdateStudentRequest request
    ) {

        StudentResponse response =
                studentService.updateStudent(
                        studentId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Student By ID",
            description = "Fetch student using student ID"
    )
    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN','STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(
            @PathVariable Long studentId
    ) {

        StudentResponse response =
                studentService.getStudentById(studentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get All Students",
            description = "Fetch paginated list of students"
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {

        PageResponse<StudentResponse> response =
                studentService.getAllStudents(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Students fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Search Students",
            description = "Search students by keyword"
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> searchStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<StudentResponse> response =
                studentService.searchStudents(
                        keyword,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Students fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Students By College",
            description = "Fetch students by college ID"
    )
    @GetMapping("/college/{collegeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentsByCollege(
            @PathVariable Long collegeId
    ) {

        List<StudentResponse> response =
                studentService.getStudentsByCollege(collegeId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Students fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Activate Student",
            description = "Activate student account"
    )
    @PatchMapping("/{studentId}/activate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> activateStudent(
            @PathVariable Long studentId
    ) {

        StudentResponse response =
                studentService.activateStudent(studentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student activated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Deactivate Student",
            description = "Deactivate student account"
    )
    @PatchMapping("/{studentId}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> deactivateStudent(
            @PathVariable Long studentId
    ) {

        StudentResponse response =
                studentService.deactivateStudent(studentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student deactivated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Check Student Email Exists",
            description = "Validate whether email already exists"
    )
    @GetMapping("/exists/email")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmail(
            @RequestParam String email
    ) {

        Boolean response =
                studentService.existsByEmail(email);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Email existence checked successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Check Roll Number Exists",
            description = "Validate whether roll number already exists"
    )
    @GetMapping("/exists/roll-number")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> existsByRollNumber(
            @RequestParam String rollNumber
    ) {

        Boolean response =
                studentService.existsByRollNumber(rollNumber);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Roll number existence checked successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Soft Delete Student",
            description = "Soft delete student account"
    )
    @DeleteMapping("/{studentId}/soft")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteStudent(
            @PathVariable Long studentId
    ) {

        studentService.softDeleteStudent(studentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student soft deleted successfully"
                )
        );
    }

    @Operation(
            summary = "Permanent Delete Student",
            description = "Permanently delete student"
    )
    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @PathVariable Long studentId
    ) {

        studentService.deleteStudent(studentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Student deleted successfully"
                )
        );
    }

    @Operation(
            summary = "Get Current Student Profile",
            description = "Fetch logged-in student profile"
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> getCurrentStudentProfile() {

        StudentResponse response =
                studentService.getCurrentStudentProfile();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current student profile fetched successfully",
                        response
                )
        );
    }
}