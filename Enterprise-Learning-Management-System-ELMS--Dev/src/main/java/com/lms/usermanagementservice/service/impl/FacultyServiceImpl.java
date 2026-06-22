package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.dto.response.FacultyResponse;
import com.lms.usermanagementservice.entity.FacultyProfile;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.repository.FacultyProfileRepository;
import com.lms.usermanagementservice.service.FacultyService;
import com.lms.usermanagementservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyServiceImpl implements FacultyService {
    private final FacultyProfileRepository repository;

    @Override
    @Transactional(readOnly = true)
    public FacultyResponse getFaculty(Long facultyId) {
        FacultyProfile profile = repository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        User current = SecurityUtil.getCurrentUser();
        boolean superAdmin = current.getUserRoles().stream().anyMatch(role ->
                role.getRole().getName().replace("ROLE_", "").equals(UserRoleType.SUPER_ADMIN.name()));
        boolean owner = current.getId().equals(profile.getUser().getId())
                || current.getId().equals(profile.getUser().getCollegeAdminId());
        if (!superAdmin && !owner) throw new ForbiddenException("Access denied");
        return map(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyResponse> getScopedFaculty() {
        User current = SecurityUtil.getCurrentUser();
        boolean superAdmin = current.getUserRoles().stream().anyMatch(role ->
                role.getRole().getName().replace("ROLE_", "").equals(UserRoleType.SUPER_ADMIN.name()));
        List<FacultyProfile> profiles = superAdmin ? repository.findAll()
                : repository.findAllByUserCollegeAdmin_Id(current.getId());
        return profiles.stream().map(this::map).toList();
    }

    private FacultyResponse map(FacultyProfile profile) {
        return FacultyResponse.builder().id(profile.getId()).userId(profile.getUser().getId())
                .facultyId(profile.getFacultyId()).firstName(profile.getUser().getFirstName())
                .lastName(profile.getUser().getLastName()).email(profile.getUser().getEmail())
                .designation(profile.getDesignation()).department(profile.getDepartment())
                .collegeAdminId(profile.getUser().getCollegeAdminId())
                .collegeCode(profile.getCollege() == null ? null : profile.getCollege().getCollegeCode()).build();
    }
}
