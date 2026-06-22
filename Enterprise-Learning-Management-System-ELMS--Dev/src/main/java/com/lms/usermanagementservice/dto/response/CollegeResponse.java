package com.lms.usermanagementservice.dto.response;

import com.lms.usermanagementservice.enums.CollegeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeResponse {

    private Long collegeId;

    private String collegeName;

    private String collegeCode;

    private String email;

    private String phoneNumber;

    private String address;

    private CollegeStatus collegeStatus;
}