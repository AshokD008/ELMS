package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.entity.Verification;
import com.lms.usermanagementservice.enums.VerificationStatus;

import java.util.List;

public interface VerificationService {

    Verification createVerification(
            Long userId,
            String documentType,
            String documentNumber,
            String documentUrl
    );

    Verification getVerificationById(
            Long verificationId
    );

    List<Verification> getUserVerifications(
            Long userId
    );

    PageResponse<Verification> getAllVerifications(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    PageResponse<Verification> searchVerifications(
            String keyword,
            int page,
            int size
    );

    Verification approveVerification(
            Long verificationId,
            String remarks
    );

    Verification rejectVerification(
            Long verificationId,
            String remarks
    );

    Verification markVerificationUnderReview(
            Long verificationId,
            String remarks
    );

    VerificationStatus getVerificationStatus(
            Long userId
    );

    Boolean isUserVerified(Long userId);

    void deleteVerification(Long verificationId);

    void softDeleteVerification(Long verificationId);
}