package com.internhub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
public class StudentProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;
    private String phone;
    private String college;
    private String degree;
    private Integer graduationYear;
    private String skills;
    private String location;
    private String github;
    private String linkedin;
    private String resumeUrl;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
}
