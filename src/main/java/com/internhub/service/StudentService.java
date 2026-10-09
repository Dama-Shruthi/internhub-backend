package com.internhub.service;

import com.internhub.entity.*;
import com.internhub.repository.*;
import com.internhub.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedInternshipRepository savedInternshipRepository;
    private final InternshipRepository internshipRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public StudentService(StudentProfileRepository studentProfileRepository,
                          ApplicationRepository applicationRepository,
                          SavedInternshipRepository savedInternshipRepository,
                          InternshipRepository internshipRepository,
                          NotificationRepository notificationRepository,
                          UserRepository userRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.applicationRepository = applicationRepository;
        this.savedInternshipRepository = savedInternshipRepository;
        this.internshipRepository = internshipRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public StudentProfile getProfile(Authentication auth) {
        User user = getUser(auth);
        return studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
    }

    @Transactional
    public StudentProfile updateProfile(Authentication auth, StudentProfile updated) {
        StudentProfile profile = getProfile(auth);
        if (updated.getName() != null) profile.setName(updated.getName());
        if (updated.getPhone() != null) profile.setPhone(updated.getPhone());
        if (updated.getCollege() != null) profile.setCollege(updated.getCollege());
        if (updated.getDegree() != null) profile.setDegree(updated.getDegree());
        if (updated.getGraduationYear() != null) profile.setGraduationYear(updated.getGraduationYear());
        if (updated.getSkills() != null) profile.setSkills(updated.getSkills());
        if (updated.getLocation() != null) profile.setLocation(updated.getLocation());
        if (updated.getGithub() != null) profile.setGithub(updated.getGithub());
        if (updated.getLinkedin() != null) profile.setLinkedin(updated.getLinkedin());
        if (updated.getSummary() != null) profile.setSummary(updated.getSummary());
        return studentProfileRepository.save(profile);
    }

    @Transactional
    public StudentProfile updateResumeUrl(Authentication auth, String resumeUrl) {
        StudentProfile profile = getProfile(auth);
        profile.setResumeUrl(resumeUrl);
        return studentProfileRepository.save(profile);
    }

    public Map<String, Object> getDashboardStats(Authentication auth) {
        StudentProfile student = getProfile(auth);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalApplications", applicationRepository.countByStudent(student));
        stats.put("shortlisted", applicationRepository.countByStudentAndStatus(student, ApplicationStatus.SHORTLISTED));
        stats.put("selected", applicationRepository.countByStudentAndStatus(student, ApplicationStatus.SELECTED));
        stats.put("rejected", applicationRepository.countByStudentAndStatus(student, ApplicationStatus.REJECTED));
        stats.put("saved", savedInternshipRepository.countByStudent(student));
        stats.put("profileCompletion", calculateProfileCompletion(student));
        stats.put("name", student.getName() != null ? student.getName() : getUser(auth).getEmail());
        stats.put("unreadNotifications", notificationRepository.countByUserIdAndReadStatusFalse(getUser(auth).getId()));
        return stats;
    }

    @Transactional
    public Application applyToInternship(Authentication auth, Long internshipId) {
        StudentProfile student = getProfile(auth);
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new IllegalArgumentException("Internship not found"));

        if (applicationRepository.existsByStudentAndInternship(student, internship)) {
            throw new IllegalArgumentException("You have already applied to this internship");
        }
        if (internship.getStatus() != InternshipStatus.PUBLISHED) {
            throw new IllegalArgumentException("This internship is not accepting applications");
        }
        if (internship.getDeadline() != null && internship.getDeadline().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Application deadline has passed");
        }

        Application application = new Application();
        application.setStudent(student);
        application.setInternship(internship);
        application.setStatus(ApplicationStatus.APPLIED);
        Application saved = applicationRepository.save(application);

        // Notify company
        createNotification(internship.getCompany().getUser(),
                "New application received for: " + internship.getTitle(), "APPLICATION");
        return saved;
    }

    @Transactional
    public void withdrawApplication(Authentication auth, Long applicationId) {
        StudentProfile student = getProfile(auth);
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("Access denied");
        }
        if (application.getStatus() == ApplicationStatus.SELECTED) {
            throw new IllegalArgumentException("Cannot withdraw a selected application");
        }
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    public List<Application> getMyApplications(Authentication auth) {
        StudentProfile student = getProfile(auth);
        return applicationRepository.findByStudent(student);
    }

    @Transactional
    public SavedInternship saveInternship(Authentication auth, Long internshipId) {
        StudentProfile student = getProfile(auth);
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new IllegalArgumentException("Internship not found"));

        if (savedInternshipRepository.existsByStudentAndInternship(student, internship)) {
            throw new IllegalArgumentException("Already saved");
        }
        SavedInternship saved = new SavedInternship();
        saved.setStudent(student);
        saved.setInternship(internship);
        return savedInternshipRepository.save(saved);
    }

    @Transactional
    public void unsaveInternship(Authentication auth, Long internshipId) {
        StudentProfile student = getProfile(auth);
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new IllegalArgumentException("Internship not found"));
        SavedInternship saved = savedInternshipRepository.findByStudentAndInternship(student, internship)
                .orElseThrow(() -> new IllegalArgumentException("Not saved"));
        savedInternshipRepository.delete(saved);
    }

    public List<SavedInternship> getSavedInternships(Authentication auth) {
        StudentProfile student = getProfile(auth);
        return savedInternshipRepository.findByStudent(student);
    }

    private int calculateProfileCompletion(StudentProfile p) {
        int score = 0;
        if (p.getName() != null && !p.getName().isEmpty()) score += 15;
        if (p.getPhone() != null && !p.getPhone().isEmpty()) score += 10;
        if (p.getCollege() != null && !p.getCollege().isEmpty()) score += 15;
        if (p.getDegree() != null && !p.getDegree().isEmpty()) score += 10;
        if (p.getSkills() != null && !p.getSkills().isEmpty()) score += 20;
        if (p.getSummary() != null && !p.getSummary().isEmpty()) score += 10;
        if (p.getGithub() != null && !p.getGithub().isEmpty()) score += 10;
        if (p.getLinkedin() != null && !p.getLinkedin().isEmpty()) score += 10;
        return score;
    }

    private void createNotification(User user, String message, String type) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setType(type);
        notificationRepository.save(n);
    }

    private User getUser(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return details.getUser();
    }
}
