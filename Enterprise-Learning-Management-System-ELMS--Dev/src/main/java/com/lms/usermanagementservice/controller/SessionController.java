package com.lms.usermanagementservice.controller;

import com.lms.usermanagementservice.dto.response.ApiResponse;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.entity.Session;
import com.lms.usermanagementservice.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(
        name = "Session Controller",
        description = "Session Management APIs"
)
@SecurityRequirement(name = "bearerAuth")
@Validated
public class SessionController {

    private final SessionService sessionService;

    @Operation(
            summary = "Create Session",
            description = "Create user session"
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Session>> createSession(
            @RequestParam @Positive Long userId,
            @RequestParam @NotBlank String accessToken,
            @RequestParam @NotBlank String ipAddress,
            @RequestParam @NotBlank String userAgent
    ) {

        Session response =
                sessionService.createSession(
                        userId,
                        accessToken,
                        ipAddress,
                        userAgent
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Session created successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Session By ID",
            description = "Fetch session by ID"
    )
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Session>> getSessionById(
            @PathVariable Long sessionId
    ) {

        Session response =
                sessionService.getSessionById(sessionId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Session fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get Session By Token",
            description = "Fetch session using access token"
    )
    @GetMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Session>> getSessionByToken(
            @RequestParam String accessToken
    ) {

        Session response =
                sessionService.getSessionByToken(
                        accessToken
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Session fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get User Sessions",
            description = "Fetch all sessions of user"
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Session>>> getUserSessions(
            @PathVariable Long userId
    ) {

        List<Session> response =
                sessionService.getUserSessions(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User sessions fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get All Sessions",
            description = "Fetch paginated sessions"
    )
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<Session>>> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {

        PageResponse<Session> response =
                sessionService.getAllSessions(
                        page,
                        size,
                        sortBy,
                        sortDirection
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Sessions fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Update Session Activity",
            description = "Update session last activity"
    )
    @PatchMapping("/activity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Session>> updateSessionActivity(
            @RequestParam String accessToken
    ) {

        Session response =
                sessionService.updateSessionActivity(
                        accessToken
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Session activity updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Logout Session",
            description = "Logout current session"
    )
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutSession(
            HttpServletRequest request,
            @RequestParam @NotBlank String refreshToken
    ) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.length() <= 7) {
            throw new com.lms.usermanagementservice.exception.InvalidTokenException(
                    "Missing or invalid Authorization header");
        }

        String accessToken =
                authHeader.substring(7);

        sessionService.logoutSession(accessToken, refreshToken);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Session logout successful"
                )
        );
    }

    @Operation(
            summary = "Logout All User Sessions",
            description = "Logout all sessions of user"
    )
    @PostMapping("/logout-all/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logoutAllUserSessions(
            @PathVariable Long userId
    ) {

        sessionService.logoutAllUserSessions(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All sessions logged out successfully"
                )
        );
    }

    @Operation(
            summary = "Expire Inactive Sessions",
            description = "Expire inactive sessions"
    )
    @PatchMapping("/expire-inactive")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> expireInactiveSessions() {

        sessionService.expireInactiveSessions();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Inactive sessions expired successfully"
                )
        );
    }

    @Operation(
            summary = "Check Session Active",
            description = "Validate whether session is active"
    )
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> isSessionActive(
            @RequestParam String accessToken
    ) {

        Boolean response =
                sessionService.isSessionActive(
                        accessToken
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Session validation completed",
                        response
                )
        );
    }

    @Operation(
            summary = "Revoke Session",
            description = "Revoke session by admin"
    )
    @PatchMapping("/{sessionId}/revoke")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @PathVariable Long sessionId
    ) {

        sessionService.revokeSession(sessionId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Session revoked successfully"
                )
        );
    }

    @Operation(
            summary = "Revoke All Sessions Except Current",
            description = "Revoke all sessions except current session"
    )
    @PatchMapping("/revoke-others/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> revokeAllSessionsExceptCurrent(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.length() <= 7) {
            throw new com.lms.usermanagementservice.exception.InvalidTokenException(
                    "Missing or invalid Authorization header");
        }

        String currentToken =
                authHeader.substring(7);

        sessionService.revokeAllSessionsExceptCurrent(
                userId,
                currentToken
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Other sessions revoked successfully"
                )
        );
    }

    @Operation(
            summary = "Get Active Session Count",
            description = "Fetch active session count"
    )
    @GetMapping("/count/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getActiveSessionCount(
            @PathVariable Long userId
    ) {

        Long response =
                sessionService.getActiveSessionCount(
                        userId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Active session count fetched successfully",
                        response
                )
        );
    }
}
