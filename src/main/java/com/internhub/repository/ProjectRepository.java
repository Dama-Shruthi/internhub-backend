package com.internhub.repository;

import com.internhub.entity.Project;
import com.internhub.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStudent(StudentProfile student);
}
