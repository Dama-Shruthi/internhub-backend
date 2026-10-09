package com.internhub.repository;

import com.internhub.entity.Internship;
import com.internhub.entity.InternshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {
    List<Internship> findByStatus(InternshipStatus status);
    List<Internship> findByCompanyId(Long companyId);
    long countByStatus(InternshipStatus status);
    List<Internship> findByStatusAndTitleContainingIgnoreCase(InternshipStatus status, String title);
    List<Internship> findByStatusAndLocationContainingIgnoreCase(InternshipStatus status, String location);
    List<Internship> findByStatusAndRequiredSkillsContainingIgnoreCase(InternshipStatus status, String skill);
}
