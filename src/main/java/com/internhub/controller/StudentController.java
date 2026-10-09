package com.internhub.controller;

import com.internhub.entity.*;
import com.internhub.service.StudentService;
import com.internhub.service.InternshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final InternshipService internshipService;

    public StudentController(StudentService studentService, InternshipService internshipService) {
        this.studentService = studentService;
        this.internshipService = internshipService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        try {
            return ResponseEntity.ok(studentService.getDashboardStats(auth));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        try {
            StudentProfile profile = studentService.getProfile(auth);
            return ResponseEntity.ok(safeProfile(profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth, @RequestBody StudentProfile updated) {
        try {
            StudentProfile profile = studentService.updateProfile(auth, updated);
            return ResponseEntity.ok(safeProfile(profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/profile/resume")
    public ResponseEntity<?> uploadResume(Authentication auth, @RequestParam("file") MultipartFile file) {
        try {
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size must be less than 5MB"));
            }
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".pdf";
            String safeFilename = UUID.randomUUID().toString() + extension;
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(safeFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "/uploads/" + safeFilename;
            
            StudentProfile updated = studentService.updateResumeUrl(auth, fileUrl);
            return ResponseEntity.ok(Map.of("resumeUrl", fileUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }

    @GetMapping("/applications")
    public ResponseEntity<?> getApplications(Authentication auth) {
        try {
            List<Application> apps = studentService.getMyApplications(auth);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Application app : apps) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", app.getId());
                m.put("internshipId", app.getInternship().getId());
                m.put("internshipTitle", app.getInternship().getTitle());
                m.put("companyName", app.getInternship().getCompany().getCompanyName());
                m.put("location", app.getInternship().getLocation());
                m.put("stipend", app.getInternship().getStipend());
                m.put("status", app.getStatus().name());
                m.put("appliedDate", app.getAppliedDate());
                result.add(m);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/apply/{internshipId}")
    public ResponseEntity<?> apply(Authentication auth, @PathVariable Long internshipId) {
        try {
            Application app = studentService.applyToInternship(auth, internshipId);
            return ResponseEntity.ok(Map.of("message", "Application submitted successfully", "applicationId", app.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/applications/{id}/withdraw")
    public ResponseEntity<?> withdraw(Authentication auth, @PathVariable Long id) {
        try {
            studentService.withdrawApplication(auth, id);
            return ResponseEntity.ok(Map.of("message", "Application withdrawn"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/save/{internshipId}")
    public ResponseEntity<?> saveInternship(Authentication auth, @PathVariable Long internshipId) {
        try {
            studentService.saveInternship(auth, internshipId);
            return ResponseEntity.ok(Map.of("message", "Internship saved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/save/{internshipId}")
    public ResponseEntity<?> unsaveInternship(Authentication auth, @PathVariable Long internshipId) {
        try {
            studentService.unsaveInternship(auth, internshipId);
            return ResponseEntity.ok(Map.of("message", "Removed from saved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/saved")
    public ResponseEntity<?> getSaved(Authentication auth) {
        try {
            List<SavedInternship> saved = studentService.getSavedInternships(auth);
            List<Map<String, Object>> result = new ArrayList<>();
            for (SavedInternship s : saved) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", s.getId());
                m.put("internshipId", s.getInternship().getId());
                m.put("internshipTitle", s.getInternship().getTitle());
                m.put("companyName", s.getInternship().getCompany().getCompanyName());
                m.put("location", s.getInternship().getLocation());
                m.put("stipend", s.getInternship().getStipend());
                m.put("status", s.getInternship().getStatus().name());
                m.put("savedDate", s.getSavedDate());
                result.add(m);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/internships/{id}/match")
    public ResponseEntity<?> getSkillMatch(Authentication auth, @PathVariable Long id) {
        try {
            StudentProfile student = studentService.getProfile(auth);
            Internship internship = internshipService.getById(id);
            int match = internshipService.calculateSkillMatch(student, internship);
            return ResponseEntity.ok(Map.of("matchPercentage", match,
                    "studentSkills", student.getSkills() != null ? student.getSkills() : "",
                    "requiredSkills", internship.getRequiredSkills() != null ? internship.getRequiredSkills() : ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> safeProfile(StudentProfile p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("phone", p.getPhone());
        m.put("college", p.getCollege());
        m.put("degree", p.getDegree());
        m.put("graduationYear", p.getGraduationYear());
        m.put("skills", p.getSkills());
        m.put("location", p.getLocation());
        m.put("github", p.getGithub());
        m.put("linkedin", p.getLinkedin());
        m.put("resumeUrl", p.getResumeUrl());
        m.put("summary", p.getSummary());
        m.put("email", p.getUser().getEmail());
        return m;
    }
}
