package com.lms.usermanagementservice.entity;

import com.lms.usermanagementservice.enums.AccountType;
import com.lms.usermanagementservice.enums.ApprovalStage;
import com.lms.usermanagementservice.enums.AuthProvider;
import com.lms.usermanagementservice.enums.Gender;
import com.lms.usermanagementservice.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "mobile_number"),
                @UniqueConstraint(columnNames = "username")
        },
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_mobile", columnList = "mobile_number"),
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_status", columnList = "status"),
                @Index(name = "idx_user_college", columnList = "college_id"),
                @Index(name = "idx_user_college_admin", columnList = "college_admin_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(name = "username", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "email", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "mobile_number", nullable = false, length = 20, unique = true)
    private String mobileNumber;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status;
    
    		@Enumerated(EnumType.STRING)
    		@Column(name = "approval_stage", length = 50)
    		private ApprovalStage approvalStage;

    		@Column(name = "approved_by_college_admin")
    		private Long approvedByCollegeAdmin;

    		@Column(name = "approved_by_super_admin")
    		private Long approvedBySuperAdmin;

    		@Column(name = "college_admin_approved_at")
    		private LocalDateTime collegeAdminApprovedAt;

    		@Column(name = "super_admin_approved_at")
    		private LocalDateTime superAdminApprovedAt;

    		@Column(name = "rejection_reason", length = 500)
    		private String rejectionReason;
    		
    				
    				

   


    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 30)
    private AuthProvider authProvider;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "mobile_verified", nullable = false)
    @Builder.Default
    private Boolean mobileVerified = false;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "lockout_until")
    private LocalDateTime lockoutUntil;

    @Column(name = "last_otp_sent_at")
    private LocalDateTime lastOtpSentAt;

    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private Boolean accountLocked = false;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_admin_id", foreignKey = @ForeignKey(name = "fk_user_college_admin"))
    private User collegeAdmin;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserRole> userRoles = new ArrayList<>();

    public Long getCollegeAdminId() {
        return collegeAdmin == null ? null : collegeAdmin.getId();
    }

    public void setCollegeAdminId(Long collegeAdminId) {
        if (collegeAdminId == null) {
            collegeAdmin = null;
            return;
        }
        User reference = new User();
        reference.setId(collegeAdminId);
        collegeAdmin = reference;
    }

    public String getPhoneNumber() {
        return mobileNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mobileNumber = phoneNumber;
    }

    public Boolean getEnabled() {
        return status == UserStatus.ACTIVE;
    }

    public void setEnabled(Boolean enabled) {
        this.status = Boolean.FALSE.equals(enabled)
                ? UserStatus.REJECTED
                : UserStatus.ACTIVE;
    }

    public Boolean getAccountNonLocked() {
        return !Boolean.TRUE.equals(accountLocked);
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountLocked = Boolean.FALSE.equals(accountNonLocked);
    }

    public String getProfileImage() {
        return profileImageUrl;
    }

    public void setProfileImage(String profileImage) {
        this.profileImageUrl = profileImage;
    }

    public Boolean getPhoneVerified() {
        return mobileVerified;
    }

    public void setProvider(AuthProvider provider) {
        this.authProvider = provider;
    }

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

    
        		if (this.status == null) {
        		    this.status = UserStatus.PENDING_VERIFICATION;
        		}

        		if (this.approvalStage == null) {
        		    this.approvalStage = ApprovalStage.NONE;
        		}
        		


        if (this.authProvider == null) {
            this.authProvider = AuthProvider.LOCAL;
        }

        if (this.accountType == null) {
            this.accountType = AccountType.STUDENT;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
