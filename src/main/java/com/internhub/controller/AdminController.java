package com.internhub.controller;

import com.internhub.entity.*;
import com.internhub.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<User> users = adminService.getAllUsers();
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("email", u.getEmail());
            m.put("role", u.getRole().name());
            m.put("active", u.isActive());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<?> toggleUser(@PathVariable Long id) {
        try {
            User user = adminService.toggleUserStatus(id);
            return ResponseEntity.ok(Map.of("message", "User status updated", "active", user.isActive()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/companies")
    public ResponseEntity<?> getCompanies() {
        List<CompanyProfile> companies = adminService.getAllCompanies();
        List<Map<String, Object>> result = companies.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("companyName", c.getCompanyName());
            m.put("email", c.getUser().getEmail());
            m.put("website", c.getWebsite());
            m.put("verificationStatus", c.getVerificationStatus());
            m.put("active", c.getUser().isActive());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/companies/{id}/verify")
    public ResponseEntity<?> verifyCompany(@PathVariable Long id) {
        try {
            adminService.verifyCompany(id);
            return ResponseEntity.ok(Map.of("message", "Company verified successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/companies/{id}/reject")
    public ResponseEntity<?> rejectCompany(@PathVariable Long id) {
        try {
            adminService.rejectCompany(id);
            return ResponseEntity.ok(Map.of("message", "Company rejected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/companies/{id}/suspend")
    public ResponseEntity<?> suspendCompany(@PathVariable Long id) {
        try {
            adminService.suspendCompany(id);
            return ResponseEntity.ok(Map.of("message", "Company suspended"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/internships")
    public ResponseEntity<?> getInternships() {
        List<Internship> internships = adminService.getAllInternships();
        List<Map<String, Object>> result = internships.stream().map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", i.getId());
            m.put("title", i.getTitle());
            m.put("company", i.getCompany().getCompanyName());
            m.put("location", i.getLocation());
            m.put("stipend", i.getStipend());
            m.put("status", i.getStatus() != null ? i.getStatus().name() : null);
            m.put("deadline", i.getDeadline());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/internships/{id}")
    public ResponseEntity<?> deleteInternship(@PathVariable Long id) {
        try {
            adminService.deleteInternship(id);
            return ResponseEntity.ok(Map.of("message", "Internship deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/applications")
    public ResponseEntity<?> getApplications() {
        List<Application> apps = adminService.getAllApplications();
        List<Map<String, Object>> result = apps.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("studentName", a.getStudent().getName());
            m.put("studentEmail", a.getStudent().getUser().getEmail());
            m.put("internshipTitle", a.getInternship().getTitle());
            m.put("company", a.getInternship().getCompany().getCompanyName());
            m.put("status", a.getStatus().name());
            m.put("appliedDate", a.getAppliedDate());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }
}
