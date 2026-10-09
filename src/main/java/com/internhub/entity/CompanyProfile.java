package com.internhub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "company_profiles")
@Getter
@Setter
public class CompanyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String companyName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String website;
    private String verificationStatus;
    private String contactInfo;

    private String industry;
    private String location;
    
    @Column(columnDefinition = "TEXT")
    private String logoUrl; // We will use a URL string or Base64 string for the logo
}
