package com.lms.usermanagementservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "student_profiles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "student_id"),
                @UniqueConstraint(columnNames = "admission_number"),
                @UniqueConstraint(columnNames = "roll_number")
        },
        indexes = {
                @Index(name = "idx_student_profile_user", columnList = "user_id"),
                @Index(name = "idx_student_profile_college", columnList = "college_id"),
                @Index(name = "idx_student_profile_admission", columnList = "admission_number"),
                @Index(name = "idx_student_profile_roll", columnList = "roll_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;

    @Column(name = "student_id", nullable = false, unique = true, length = 100)
    private String studentId;

    @Column(name = "admission_number", nullable = false, unique = true, length = 100)
    private String admissionNumber;

    @Column(name = "roll_number", nullable = false, unique = true, length = 100)
    private String rollNumber;

    @Column(name = "department", length = 150)
    private String department;

    @Column(name = "course_name", length = 150)
    private String courseName;

    @Column(name = "academic_year", length = 50)
    private String academicYear;

    @Column(name = "semester", length = 50)
    private String semester;

    @Column(name = "section_name", length = 50)
    private String sectionName;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "guardian_name", length = 150)
    private String guardianName;

    @Column(name = "guardian_mobile", length = 20)
    private String guardianMobile;

    @Column(name = "guardian_email", length = 150)
    private String guardianEmail;

    @Column(name = "blood_group", length = 20)
    private String bloodGroup;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "religion", length = 100)
    private String religion;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "aadhar_number", length = 30)
    private String aadharNumber;

    @Column(name = "address_line_1", length = 255)
    private String addressLine1;

    @Column(name = "address_line_2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "profile_completed", nullable = false)
    @Builder.Default
    private Boolean profileCompleted = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setAdmissionDate(LocalDateTime admissionDate) {
        this.joiningDate = admissionDate == null ? null : admissionDate.toLocalDate();
    }

    public String getYear() {
        return academicYear;
    }

    public void setYear(String year) {
        this.academicYear = year;
    }

    public String getSection() {
        return sectionName;
    }

    public void setSection(String section) {
        this.sectionName = section;
    }
}
