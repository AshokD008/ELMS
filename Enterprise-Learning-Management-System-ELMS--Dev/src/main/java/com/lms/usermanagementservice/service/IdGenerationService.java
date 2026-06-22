package com.lms.usermanagementservice.service;

import com.lms.usermanagementservice.entity.College;
import com.lms.usermanagementservice.repository.CollegeAdminProfileRepository;
import com.lms.usermanagementservice.repository.StudentProfileRepository;
import com.lms.usermanagementservice.repository.FacultyProfileRepository;
import com.lms.usermanagementservice.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class IdGenerationService {

    private final CollegeAdminProfileRepository collegeAdminProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final CollegeRepository collegeRepository;

    @Transactional(readOnly = true)
    public String nextCollegeAdminId() {
        String prefix = "admin";
        return prefix + String.format("%03d", collegeAdminProfileRepository.countByEmployeeIdStartingWith(prefix) + 1);
    }

    @Transactional(readOnly = true)
    public String nextFacultyId(Long collegeAdminId) {
        return "FAC" + String.format("%03d", facultyProfileRepository.countByUserCollegeAdmin_Id(collegeAdminId) + 1);
    }

    @Transactional(readOnly = true)
    public String nextCollegeStudentId(College college) {
        return nextCollegeScopedId(college, "STU", studentProfileRepository.countByCollegeId(college.getId()) + 1);
    }

    @Transactional(readOnly = true)
    public String nextIndependentStudentId(String firstName) {
        String prefix = normalizeNamePrefix(firstName);
        long nextSequence = studentProfileRepository.countByStudentIdStartingWith(prefix) + 1;
        return prefix + String.format("%03d", nextSequence);
    }

    @Transactional(readOnly = true)
    public String nextCollegeCode(String collegeName) {
        String prefix = normalizePrefix(collegeName, 4);
        return prefix + String.format("%03d", collegeRepository.countByCollegeCodeStartingWith(prefix) + 1);
    }

    private String nextCollegeScopedId(College college, String type, long sequence) {
        return college.getCollegeCode().trim().toUpperCase(Locale.ROOT)
                + type
                + String.format("%03d", sequence);
    }

    private String normalizeNamePrefix(String name) {
        String lettersOnly = StringUtils.hasText(name)
                ? name.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT)
                : "STU";

        return normalizePrefix(lettersOnly, 4);
    }

    private String normalizePrefix(String value, int length) {
        String lettersOnly = StringUtils.hasText(value)
                ? value.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT)
                : "USER";
        return (lettersOnly + "XXXX").substring(0, length);
    }
}
