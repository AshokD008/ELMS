package com.lms.usermanagementservice.entity;

import com.lms.usermanagementservice.enums.CollegeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "colleges",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "college_code"),
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "mobile_number")
        },
        indexes = {
                @Index(name = "idx_college_code", columnList = "college_code"),
                @Index(name = "idx_college_status", columnList = "status"),
                @Index(name = "idx_college_email", columnList = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "college_name", nullable = false, length = 255)
    private String collegeName;

    @Column(name = "college_code", nullable = false, unique = true, length = 50)
    private String collegeCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mobile_number", nullable = false, unique = true, length = 20)
    private String mobileNumber;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

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

    @Column(name = "principal_name", length = 150)
    private String principalName;

    @Column(name = "support_email", length = 150)
    private String supportEmail;

    @Column(name = "support_mobile", length = 20)
    private String supportMobile;

    @Column(name = "max_students_allowed")
    private Integer maxStudentsAllowed;

    @Column(name = "max_faculty_allowed")
    private Integer maxFacultyAllowed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CollegeStatus status;

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

        if (this.status == null) {
            this.status = CollegeStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getName() {
        return collegeName;
    }

    public void setName(String name) {
        this.collegeName = name;
    }

    public String getPhoneNumber() {
        return mobileNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mobileNumber = phoneNumber;
    }

    public String getAddress() {
        return addressLine1;
    }

    public void setAddress(String address) {
        this.addressLine1 = address;
    }

    public CollegeStatus getCollegeStatus() {
        return status;
    }

    public void setCollegeStatus(CollegeStatus collegeStatus) {
        this.status = collegeStatus;
    }
}
