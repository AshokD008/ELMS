
package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.ProfileResponse;
import com.lms.usermanagementservice.dto.response.SuperAdminResponse;
import com.lms.usermanagementservice.dto.response.TokenResponse;

public interface SuperAdminService {

    /*
     * =========================================
     * USER APPROVAL MANAGEMENT
     * =========================================
     */

    TokenResponse approveUser(
            Long userId
    );

    TokenResponse rejectUser(
            Long userId,
            String reason
    );

    PageResponse<ProfileResponse>
    getPendingApprovalUsers(
            int page,
            int size
    );

    /*
     * =========================================
     * USER STATUS MANAGEMENT
     * =========================================
     */

    TokenResponse activateUser(
            Long userId
    );

    TokenResponse deactivateUser(
            Long userId
    );

    TokenResponse blockUser(
            Long userId
    );

    TokenResponse unblockUser(
            Long userId
    );

    /*
     * =========================================
     * SUPER ADMIN PROFILE
     * =========================================
     */

    SuperAdminResponse
    getCurrentSuperAdminProfile();
}
