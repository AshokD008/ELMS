package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.request.CreateCollegeRequest;
import com.lms.usermanagementservice.dto.request.UpdateCollegeRequest;
import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.CollegeResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.service.CollegeService;
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
@RequestMapping("/api/v1/colleges")
@RequiredArgsConstructor
@Tag(
        name = "College Controller",
        description = "College Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class CollegeController {

    private final CollegeService collegeService;

    @Operation(
            summary = "Create College",
            description = "Create new college"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "College created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "College code or email already exists",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeResponse>> createCollege(
            @Valid @RequestBody CreateCollegeRequest request
    ) {

        CollegeResponse response =
                collegeService.createCollege(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "College created successfully",
                                response
                        )
                );
    }

    @Operation(
            summary = "Update College",
            description = "Update college details"
    )
    @PutMapping("/{collegeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeResponse>> updateCollege(
            @PathVariable Long collegeId,
            @Valid @RequestBody UpdateCollegeRequest request
    ) {

        CollegeResponse response =
                collegeService.updateCollege(
                        collegeId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get College By ID",
            description = "Fetch college by ID"
    )
    @GetMapping("/{collegeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CollegeResponse>> getCollegeById(
            @PathVariable Long collegeId
    ) {

        CollegeResponse response =
                collegeService.getCollegeById(
                        collegeId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get College By Code",
            description = "Fetch college by college code"
    )
    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CollegeResponse>> getCollegeByCode(
            @PathVariable String code
    ) {

        CollegeResponse response =
                collegeService.getCollegeByCode(code);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get All Colleges",
            description = "Fetch paginated colleges"
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<CollegeResponse>>> getAllColleges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {

        PageResponse<CollegeResponse> response =
                collegeService.getAllColleges(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Colleges fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Search Colleges",
            description = "Search colleges using keyword"
    )
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<CollegeResponse>>> searchColleges(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<CollegeResponse> response =
                collegeService.searchColleges(
                        keyword,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Colleges fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Active Colleges",
            description = "Fetch active colleges"
    )
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CollegeResponse>>> getActiveColleges() {

        List<CollegeResponse> response =
                collegeService.getActiveColleges();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Active colleges fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Activate College",
            description = "Activate college"
    )
    @PatchMapping("/{collegeId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeResponse>> activateCollege(
            @PathVariable Long collegeId
    ) {

        CollegeResponse response =
                collegeService.activateCollege(
                        collegeId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College activated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Deactivate College",
            description = "Deactivate college"
    )
    @PatchMapping("/{collegeId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeResponse>> deactivateCollege(
            @PathVariable Long collegeId
    ) {

        CollegeResponse response =
                collegeService.deactivateCollege(
                        collegeId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College deactivated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Check College Code Exists",
            description = "Validate whether college code already exists"
    )
    @GetMapping("/exists/code")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> existsByCollegeCode(
            @RequestParam String collegeCode
    ) {

        Boolean response =
                collegeService.existsByCollegeCode(
                        collegeCode
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College code validation completed",
                        response
                )
        );
    }

    @Operation(
            summary = "Check College Email Exists",
            description = "Validate whether college email already exists"
    )
    @GetMapping("/exists/email")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmail(
            @RequestParam String email
    ) {

        Boolean response =
                collegeService.existsByEmail(email);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College email validation completed",
                        response
                )
        );
    }

    @Operation(
            summary = "Soft Delete College",
            description = "Soft delete college"
    )
    @DeleteMapping("/{collegeId}/soft")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteCollege(
            @PathVariable Long collegeId
    ) {

        collegeService.softDeleteCollege(
                collegeId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College soft deleted successfully"
                )
        );
    }

    @Operation(
            summary = "Delete College",
            description = "Permanently delete college"
    )
    @DeleteMapping("/{collegeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCollege(
            @PathVariable Long collegeId
    ) {

        collegeService.deleteCollege(
                collegeId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "College deleted successfully"
                )
        );
    }
}
