package com.internhub.controller;

import com.internhub.entity.*;
import com.internhub.repository.ProjectRepository;
import com.internhub.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/student/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final StudentService studentService;

    public ProjectController(ProjectRepository projectRepository, StudentService studentService) {
        this.projectRepository = projectRepository;
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<?> getProjects(Authentication auth) {
        StudentProfile profile = studentService.getProfile(auth);
        List<Project> projects = projectRepository.findByStudent(profile);
        List<Map<String, Object>> result = projects.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("title", p.getTitle());
            m.put("description", p.getDescription());
            m.put("technologies", p.getTechnologies());
            m.put("githubUrl", p.getGithubUrl());
            m.put("demoUrl", p.getDemoUrl());
            m.put("role", p.getRole());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> addProject(Authentication auth, @RequestBody Project project) {
        StudentProfile profile = studentService.getProfile(auth);
        project.setStudent(profile);
        Project saved = projectRepository.save(project);
        return ResponseEntity.ok(Map.of("message", "Project added", "id", saved.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(Authentication auth, @PathVariable Long id) {
        StudentProfile profile = studentService.getProfile(auth);
        Project project = projectRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        if (!project.getStudent().getId().equals(profile.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        projectRepository.delete(project);
        return ResponseEntity.ok(Map.of("message", "Project deleted"));
    }
}
