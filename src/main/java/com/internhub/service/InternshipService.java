package com.internhub.service;

import com.internhub.entity.Internship;
import com.internhub.entity.InternshipStatus;
import com.internhub.entity.StudentProfile;
import com.internhub.repository.InternshipRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InternshipService {

    private final InternshipRepository internshipRepository;

    public InternshipService(InternshipRepository internshipRepository) {
        this.internshipRepository = internshipRepository;
    }

    public List<Internship> searchInternships(String keyword, String location, String skill) {
        if (keyword != null && !keyword.isBlank()) {
            return internshipRepository.findByStatusAndTitleContainingIgnoreCase(InternshipStatus.PUBLISHED, keyword);
        }
        if (location != null && !location.isBlank()) {
            return internshipRepository.findByStatusAndLocationContainingIgnoreCase(InternshipStatus.PUBLISHED, location);
        }
        if (skill != null && !skill.isBlank()) {
            return internshipRepository.findByStatusAndRequiredSkillsContainingIgnoreCase(InternshipStatus.PUBLISHED, skill);
        }
        return internshipRepository.findByStatus(InternshipStatus.PUBLISHED);
    }

    public Internship getById(Long id) {
        return internshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Internship not found"));
    }

    public int calculateSkillMatch(StudentProfile student, Internship internship) {
        if (student.getSkills() == null || student.getSkills().isBlank()
                || internship.getRequiredSkills() == null || internship.getRequiredSkills().isBlank()) {
            return 0;
        }
        String[] studentSkills = student.getSkills().toLowerCase().split("[,;]+");
        String[] requiredSkills = internship.getRequiredSkills().toLowerCase().split("[,;]+");

        int matched = 0;
        for (String req : requiredSkills) {
            String reqTrim = req.trim();
            for (String stud : studentSkills) {
                if (stud.trim().contains(reqTrim) || reqTrim.contains(stud.trim())) {
                    matched++;
                    break;
                }
            }
        }
        return requiredSkills.length == 0 ? 0 : (int) Math.round(((double) matched / requiredSkills.length) * 100);
    }
}
