package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.request.CreateCollegeAdminRequest;
import com.lms.usermanagementservice.dto.request.UpdateCollegeAdminRequest;
import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.CollegeAdminResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.ProfileResponse;
import com.lms.usermanagementservice.dto.response.TokenResponse;
import com.lms.usermanagementservice.service.CollegeAdminService;
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
@RequestMapping("/api/v1/college-admins")
@RequiredArgsConstructor
@Tag(
        name = "College Admin Controller",
        description = "College Admin Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class CollegeAdminController {

    private final CollegeAdminService collegeAdminService;

    @GetMapping("/{adminId}/approvals")
    @PreAuthorize("hasRole('COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ProfileResponse>>> getFacultyAndStudentApprovals(
            @PathVariable Long adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Pending college users fetched successfully",
                collegeAdminService.getFacultyAndStudentApprovals(adminId, page, size)));
    }

    @PatchMapping("/{adminId}/approvals/{userId}/approve")
    @PreAuthorize("hasRole('COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<TokenResponse>> approveCollegeUser(
            @PathVariable Long adminId, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("College user approved successfully",
                collegeAdminService.approveCollegeUser(adminId, userId)));
    }

    @Operation(
            summary = "Create College Admin",
            description = "Create new college admin"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "College admin created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeAdminResponse>> createCollegeAdmin(
            @Valid @RequestBody CreateCollegeAdminRequest request
    ) {

        CollegeAdminResponse response =
                collegeAdminService.createCollegeAdmin(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "College admin created successfully",
                                response
                        )
                );
    }

    @Operation(
            summary = "Update College Admin",
            description = "Update college admin details"
    )
    @PutMapping("/{adminId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeAdminResponse>> updateCollegeAdmin(
            @PathVariable Long adminId,
            @Valid @RequestBody UpdateCollegeAdminRequest request
    ) {

        CollegeAdminResponse response =
                collegeAdminService.updateCollegeAdmin(
                        adminId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College admin updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get College Admin By ID",
            description = "Fetch college admin by ID"
    )
    @GetMapping("/{adminId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeAdminResponse>> getCollegeAdminById(
            @PathVariable Long adminId
    ) {

        CollegeAdminResponse response =
                collegeAdminService.getCollegeAdminById(adminId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College admin fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get All College Admins",
            description = "Fetch paginated list of college admins"
    )
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CollegeAdminResponse>>> getAllCollegeAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {

        PageResponse<CollegeAdminResponse> response =
                collegeAdminService.getAllCollegeAdmins(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College admins fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Search College Admins",
            description = "Search college admins using keyword"
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CollegeAdminResponse>>> searchCollegeAdmins(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<CollegeAdminResponse> response =
                collegeAdminService.searchCollegeAdmins(
                        keyword,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College admins fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get College Admins By College",
            description = "Fetch all admins by college ID"
    )
    @GetMapping("/college/{collegeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<List<CollegeAdminResponse>>> getCollegeAdminsByCollege(
            @PathVariable Long collegeId
    ) {

        List<CollegeAdminResponse> response =
                collegeAdminService.getCollegeAdminsByCollege(
                        collegeId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College admins fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Activate College Admin",
            description = "Activate college admin account"
    )
    @PatchMapping("/{adminId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeAdminResponse>> activateCollegeAdmin(
            @PathVariable Long adminId
    ) {

        CollegeAdminResponse response =
                collegeAdminService.activateCollegeAdmin(adminId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College admin activated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Deactivate College Admin",
            description = "Deactivate college admin account"
    )
    @PatchMapping("/{adminId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeAdminResponse>> deactivateCollegeAdmin(
            @PathVariable Long adminId
    ) {

        CollegeAdminResponse response =
                collegeAdminService.deactivateCollegeAdmin(adminId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College admin deactivated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Check Email Exists",
            description = "Validate whether email already exists"
    )
    @GetMapping("/exists/email")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmail(
            @RequestParam String email
    ) {

        Boolean response =
                collegeAdminService.existsByEmail(email);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Email existence checked successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Soft Delete College Admin",
            description = "Soft delete college admin"
    )
    @DeleteMapping("/{adminId}/soft")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteCollegeAdmin(
            @PathVariable Long adminId
    ) {

        collegeAdminService.softDeleteCollegeAdmin(adminId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College admin soft deleted successfully"
                )
        );
    }

    @Operation(
            summary = "Delete College Admin",
            description = "Permanently delete college admin"
    )
    @DeleteMapping("/{adminId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCollegeAdmin(
            @PathVariable Long adminId
    ) {

        collegeAdminService.deleteCollegeAdmin(adminId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College admin deleted successfully"
                )
        );
    }

    @Operation(
            summary = "Get Current College Admin Profile",
            description = "Fetch logged-in college admin profile"
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeAdminResponse>> getCurrentCollegeAdminProfile() {

        CollegeAdminResponse response =
                collegeAdminService.getCurrentCollegeAdminProfile();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current college admin profile fetched successfully",
                        response
                )
        );
    }
}
