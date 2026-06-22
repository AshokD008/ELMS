package com.lms.usermanagementservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "college_admin_profiles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "employee_id")
        },
        indexes = {
                @Index(name = "idx_college_admin_user", columnList = "user_id"),
                @Index(name = "idx_college_admin_college", columnList = "college_id"),
                @Index(name = "idx_college_admin_employee", columnList = "employee_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollegeAdminProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;

    @Column(name = "employee_id", nullable = false, unique = true, length = 100)
    private String employeeId;

    @Column(name = "designation", length = 150)
    private String designation;

    @Column(name = "department", length = 150)
    private String department;

    @Column(name = "office_mobile", length = 20)
    private String officeMobile;

    @Column(name = "office_email", length = 150)
    private String officeEmail;

    @Column(name = "office_address", length = 500)
    private String officeAddress;

    @Column(name = "joining_date")
    private LocalDateTime joiningDate;

    @Column(name = "access_level", length = 100)
    private String accessLevel;

    @Column(name = "can_manage_students", nullable = false)
    @Builder.Default
    private Boolean canManageStudents = true;

    @Column(name = "can_manage_faculty", nullable = false)
    @Builder.Default
    private Boolean canManageFaculty = true;

    @Column(name = "can_manage_courses", nullable = false)
    @Builder.Default
    private Boolean canManageCourses = true;

    @Column(name = "can_manage_settings", nullable = false)
    @Builder.Default
    private Boolean canManageSettings = false;

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
}
