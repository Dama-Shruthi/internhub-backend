package com.internhub.repository;

import com.internhub.entity.Application;
import com.internhub.entity.ApplicationStatus;
import com.internhub.entity.StudentProfile;
import com.internhub.entity.Internship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudent(StudentProfile student);
    List<Application> findByInternship(Internship internship);
    List<Application> findByStudentAndStatus(StudentProfile student, ApplicationStatus status);
    Optional<Application> findByStudentAndInternship(StudentProfile student, Internship internship);
    boolean existsByStudentAndInternship(StudentProfile student, Internship internship);
    long countByStudent(StudentProfile student);
    long countByStudentAndStatus(StudentProfile student, ApplicationStatus status);
    long countByInternship(Internship internship);
    List<Application> findByInternship_Company_Id(Long companyId);
}
