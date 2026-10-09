package com.internhub.controller;

import com.internhub.entity.Internship;
import com.internhub.service.InternshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final InternshipService internshipService;

    public PublicController(InternshipService internshipService) {
        this.internshipService = internshipService;
    }

    @GetMapping("/internships")
    public ResponseEntity<?> browseInternships(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skill) {
        List<Internship> internships = internshipService.searchInternships(keyword, location, skill);
        List<Map<String, Object>> result = internships.stream().map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", i.getId());
            m.put("title", i.getTitle());
            m.put("companyName", i.getCompany().getCompanyName());
            m.put("location", i.getLocation());
            m.put("stipend", i.getStipend());
            m.put("requiredSkills", i.getRequiredSkills());
            m.put("deadline", i.getDeadline());
            m.put("status", i.getStatus().name());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/internships/{id}")
    public ResponseEntity<?> getInternship(@PathVariable Long id) {
        try {
            Internship i = internshipService.getById(id);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", i.getId());
            m.put("title", i.getTitle());
            m.put("description", i.getDescription());
            m.put("companyName", i.getCompany().getCompanyName());
            m.put("companyId", i.getCompany().getId());
            m.put("companyWebsite", i.getCompany().getWebsite());
            m.put("location", i.getLocation());
            m.put("stipend", i.getStipend());
            m.put("requiredSkills", i.getRequiredSkills());
            m.put("deadline", i.getDeadline());
            m.put("status", i.getStatus().name());
            m.put("createdDate", i.getCreatedDate());
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
