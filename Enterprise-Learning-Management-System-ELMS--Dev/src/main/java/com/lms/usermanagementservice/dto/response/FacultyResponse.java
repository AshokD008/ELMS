package com.lms.usermanagementservice.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyResponse {
    private Long id;
    private Long userId;
    private String facultyId;
    private String firstName;
    private String lastName;
    private String email;
    private String designation;
    private String department;
    private Long collegeAdminId;
    private String collegeCode;
}
