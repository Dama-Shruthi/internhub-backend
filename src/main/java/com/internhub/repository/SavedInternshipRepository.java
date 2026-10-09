package com.internhub.repository;

import com.internhub.entity.SavedInternship;
import com.internhub.entity.StudentProfile;
import com.internhub.entity.Internship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedInternshipRepository extends JpaRepository<SavedInternship, Long> {
    List<SavedInternship> findByStudent(StudentProfile student);
    Optional<SavedInternship> findByStudentAndInternship(StudentProfile student, Internship internship);
    boolean existsByStudentAndInternship(StudentProfile student, Internship internship);
    long countByStudent(StudentProfile student);
}
