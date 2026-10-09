package com.internhub.controller;

import com.internhub.entity.*;
import com.internhub.service.CompanyService;
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
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        try {
            return ResponseEntity.ok(companyService.getDashboardStats(auth));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        try {
            CompanyProfile p = companyService.getProfile(auth);
            return ResponseEntity.ok(safeCompany(p));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth, @RequestBody CompanyProfile updated) {
        try {
            CompanyProfile p = companyService.updateProfile(auth, updated);
            return ResponseEntity.ok(safeCompany(p));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/profile/logo")
    public ResponseEntity<?> uploadLogo(Authentication auth, @RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
            }
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size must be less than 2MB"));
            }
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".png";
            String safeFilename = UUID.randomUUID().toString() + extension;
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(safeFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "/uploads/" + safeFilename;
            
            CompanyProfile updated = companyService.updateLogo(auth, fileUrl);
            return ResponseEntity.ok(Map.of("logoUrl", fileUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }

    @GetMapping("/internships")
    public ResponseEntity<?> getInternships(Authentication auth) {
        try {
            List<Internship> internships = companyService.getMyInternships(auth);
            return ResponseEntity.ok(internships.stream().map(this::safeInternship).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/internships")
    public ResponseEntity<?> createInternship(Authentication auth, @RequestBody Internship internship) {
        try {
            Internship created = companyService.createInternship(auth, internship);
            return ResponseEntity.ok(safeInternship(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/internships/{id}")
    public ResponseEntity<?> updateInternship(Authentication auth, @PathVariable Long id, @RequestBody Internship updated) {
        try {
            Internship saved = companyService.updateInternship(auth, id, updated);
            return ResponseEntity.ok(safeInternship(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/internships/{id}/publish")
    public ResponseEntity<?> publish(Authentication auth, @PathVariable Long id) {
        try {
            Internship published = companyService.publishInternship(auth, id);
            return ResponseEntity.ok(Map.of("message", "Internship published", "status", published.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/internships/{id}/close")
    public ResponseEntity<?> close(Authentication auth, @PathVariable Long id) {
        try {
            companyService.closeInternship(auth, id);
            return ResponseEntity.ok(Map.of("message", "Internship closed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/internships/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        try {
            companyService.deleteInternship(auth, id);
            return ResponseEntity.ok(Map.of("message", "Internship deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/internships/{id}/applicants")
    public ResponseEntity<?> getApplicants(Authentication auth, @PathVariable Long id) {
        try {
            List<Application> apps = companyService.getApplicants(auth, id);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Application app : apps) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("applicationId", app.getId());
                m.put("studentId", app.getStudent().getId());
                m.put("studentName", app.getStudent().getName());
                m.put("college", app.getStudent().getCollege());
                m.put("degree", app.getStudent().getDegree());
                m.put("skills", app.getStudent().getSkills());
                m.put("github", app.getStudent().getGithub());
                m.put("linkedin", app.getStudent().getLinkedin());
                m.put("status", app.getStatus().name());
                m.put("appliedDate", app.getAppliedDate());
                result.add(m);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<?> updateStatus(Authentication auth, @PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            Application app = companyService.updateApplicationStatus(auth, id, status);
            return ResponseEntity.ok(Map.of("message", "Status updated to " + status, "applicationId", app.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> safeCompany(CompanyProfile p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("companyName", p.getCompanyName());
        m.put("industry", p.getIndustry());
        m.put("location", p.getLocation());
        m.put("description", p.getDescription());
        m.put("website", p.getWebsite());
        m.put("contactInfo", p.getContactInfo());
        m.put("verificationStatus", p.getVerificationStatus());
        m.put("logoUrl", p.getLogoUrl());
        m.put("email", p.getUser().getEmail());
        return m;
    }

    private Map<String, Object> safeInternship(Internship i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("title", i.getTitle());
        m.put("description", i.getDescription());
        m.put("location", i.getLocation());
        m.put("stipend", i.getStipend());
        m.put("requiredSkills", i.getRequiredSkills());
        m.put("deadline", i.getDeadline());
        m.put("status", i.getStatus() != null ? i.getStatus().name() : null);
        m.put("createdDate", i.getCreatedDate());
        return m;
    }
}
