package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.entity.Verification;
import com.lms.usermanagementservice.enums.VerificationStatus;
import com.lms.usermanagementservice.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/verifications")
@RequiredArgsConstructor
@Tag(
        name = "Verification Controller",
        description = "Verification Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class VerificationController {

    private final VerificationService verificationService;

    @Operation(
            summary = "Create Verification",
            description = "Submit verification request"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Verification created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid verification request",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Verification>> createVerification(
            @RequestParam Long userId,
            @RequestParam String documentType,
            @RequestParam String documentNumber,
            @RequestParam String documentUrl
    ) {

        Verification response =
                verificationService.createVerification(
                        userId,
                        documentType,
                        documentNumber,
                        documentUrl
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Verification submitted successfully",
                                response
                        )
                );
    }

    @Operation(
            summary = "Get Verification By ID",
            description = "Fetch verification by ID"
    )
    @GetMapping("/{verificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Verification>> getVerificationById(
            @PathVariable Long verificationId
    ) {

        Verification response =
                verificationService.getVerificationById(
                        verificationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get User Verifications",
            description = "Fetch all verifications of user"
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Verification>>> getUserVerifications(
            @PathVariable Long userId
    ) {

        List<Verification> response =
                verificationService.getUserVerifications(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User verifications fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get All Verifications",
            description = "Fetch paginated verifications"
    )
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<Verification>>> getAllVerifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {

        PageResponse<Verification> response =
                verificationService.getAllVerifications(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verifications fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Search Verifications",
            description = "Search verifications using keyword"
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<Verification>>> searchVerifications(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<Verification> response =
                verificationService.searchVerifications(
                        keyword,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verifications fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Approve Verification",
            description = "Approve verification request"
    )
    @PatchMapping("/{verificationId}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Verification>> approveVerification(
            @PathVariable Long verificationId,
            @RequestParam(required = false)
            String remarks
    ) {

        Verification response =
                verificationService.approveVerification(
                        verificationId,
                        remarks
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification approved successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Reject Verification",
            description = "Reject verification request"
    )
    @PatchMapping("/{verificationId}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Verification>> rejectVerification(
            @PathVariable Long verificationId,
            @RequestParam(required = false)
            String remarks
    ) {

        Verification response =
                verificationService.rejectVerification(
                        verificationId,
                        remarks
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification rejected successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Mark Verification Under Review",
            description = "Mark verification under review"
    )
    @PatchMapping("/{verificationId}/under-review")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Verification>> markVerificationUnderReview(
            @PathVariable Long verificationId,
            @RequestParam(required = false)
            String remarks
    ) {

        Verification response =
                verificationService.markVerificationUnderReview(
                        verificationId,
                        remarks
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification marked under review successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Verification Status",
            description = "Fetch verification status of user"
    )
    @GetMapping("/status/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VerificationStatus>> getVerificationStatus(
            @PathVariable Long userId
    ) {

        VerificationStatus response =
                verificationService.getVerificationStatus(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification status fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Check User Verified",
            description = "Validate whether user is verified"
    )
    @GetMapping("/is-verified/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> isUserVerified(
            @PathVariable Long userId
    ) {

        Boolean response =
                verificationService.isUserVerified(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User verification validation completed",
                        response
                )
        );
    }

    @Operation(
            summary = "Soft Delete Verification",
            description = "Soft delete verification"
    )
    @DeleteMapping("/{verificationId}/soft")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteVerification(
            @PathVariable Long verificationId
    ) {

        verificationService.softDeleteVerification(
                verificationId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification soft deleted successfully"
                )
        );
    }

    @Operation(
            summary = "Delete Verification",
            description = "Permanently delete verification"
    )
    @DeleteMapping("/{verificationId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVerification(
            @PathVariable Long verificationId
    ) {

        verificationService.deleteVerification(
                verificationId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification deleted successfully"
                )
        );
    }
}