package com.internhub.repository;

import com.internhub.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    Optional<CompanyProfile> findByUserId(Long userId);
    List<CompanyProfile> findByVerificationStatus(String status);
    long countByVerificationStatus(String status);
}
