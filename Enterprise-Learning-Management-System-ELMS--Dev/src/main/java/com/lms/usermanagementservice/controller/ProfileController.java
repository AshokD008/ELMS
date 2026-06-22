package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.request.UpdateProfileRequest;
import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.ProfileResponse;
import com.lms.usermanagementservice.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(
        name = "Profile Controller",
        description = "Profile Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(
            summary = "Get Current Profile",
            description = "Fetch currently logged-in user profile"
    )
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentProfile() {

        ProfileResponse response =
                profileService.getCurrentProfile();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current profile fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Profile By User ID",
            description = "Fetch profile using user ID"
    )
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileByUserId(
            @PathVariable Long userId
    ) {

        ProfileResponse response =
                profileService.getProfileByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Update Profile",
            description = "Update logged-in user profile"
    )
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {

        ProfileResponse response =
                profileService.updateProfile(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Upload Profile Picture",
            description = "Upload user profile picture"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile picture uploaded successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping(
            value = "/upload-picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponse>> uploadProfilePicture(
            @RequestPart("file") MultipartFile file
    ) {

        ProfileResponse response =
                profileService.uploadProfilePicture(file);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile picture uploaded successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Remove Profile Picture",
            description = "Remove current profile picture"
    )
    @DeleteMapping("/remove-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeProfilePicture() {

        profileService.removeProfilePicture();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile picture removed successfully"
                )
        );
    }

    @Operation(
            summary = "Update Email",
            description = "Update user email"
    )
    @PatchMapping("/{userId}/email")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateEmail(
            @PathVariable Long userId,
            @RequestParam String email
    ) {

        ProfileResponse response =
                profileService.updateEmail(
                        userId,
                        email
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Email updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Update Phone Number",
            description = "Update user phone number"
    )
    @PatchMapping("/{userId}/phone-number")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> updatePhoneNumber(
            @PathVariable Long userId,
            @RequestParam String phoneNumber
    ) {

        ProfileResponse response =
                profileService.updatePhoneNumber(
                        userId,
                        phoneNumber
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Phone number updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Activate Profile",
            description = "Activate user profile"
    )
    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> activateProfile(
            @PathVariable Long userId
    ) {

        ProfileResponse response =
                profileService.activateProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile activated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Deactivate Profile",
            description = "Deactivate user profile"
    )
    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ProfileResponse>> deactivateProfile(
            @PathVariable Long userId
    ) {

        ProfileResponse response =
                profileService.deactivateProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile deactivated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Search Profiles",
            description = "Search profiles using keyword"
    )
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ProfileResponse>>> searchProfiles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<ProfileResponse> response =
                profileService.searchProfiles(
                        keyword,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profiles fetched successfully",
                        response
                )
        );
    }
}