package com.lms.usermanagementservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "faculty_profiles", indexes = {
        @Index(name = "idx_faculty_user", columnList = "user_id"),
        @Index(name = "idx_faculty_college", columnList = "college_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;

    @Column(name = "faculty_id", nullable = false, unique = true, length = 100)
    private String facultyId;

    @Column(name = "designation", length = 150)
    private String designation;

    @Column(name = "department", length = 150)
    private String department;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
