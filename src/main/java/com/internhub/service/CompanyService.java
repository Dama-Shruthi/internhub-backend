package com.internhub.service;

import com.internhub.entity.*;
import com.internhub.repository.*;
import com.internhub.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CompanyService {

    private final CompanyProfileRepository companyProfileRepository;
    private final InternshipRepository internshipRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyProfileRepository companyProfileRepository,
                          InternshipRepository internshipRepository,
                          ApplicationRepository applicationRepository,
                          NotificationRepository notificationRepository,
                          UserRepository userRepository) {
        this.companyProfileRepository = companyProfileRepository;
        this.internshipRepository = internshipRepository;
        this.applicationRepository = applicationRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public CompanyProfile getProfile(Authentication auth) {
        User user = getUser(auth);
        return companyProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Company profile not found"));
    }

    @Transactional
    public CompanyProfile updateProfile(Authentication auth, CompanyProfile updated) {
        CompanyProfile profile = getProfile(auth);
        if (updated.getCompanyName() != null) profile.setCompanyName(updated.getCompanyName());
        if (updated.getDescription() != null) profile.setDescription(updated.getDescription());
        if (updated.getWebsite() != null) profile.setWebsite(updated.getWebsite());
        if (updated.getContactInfo() != null) profile.setContactInfo(updated.getContactInfo());
        if (updated.getIndustry() != null) profile.setIndustry(updated.getIndustry());
        if (updated.getLocation() != null) profile.setLocation(updated.getLocation());
        if (updated.getLogoUrl() != null) profile.setLogoUrl(updated.getLogoUrl());
        return companyProfileRepository.save(profile);
    }

    @Transactional
    public CompanyProfile updateLogo(Authentication auth, String logoUrl) {
        CompanyProfile profile = getProfile(auth);
        profile.setLogoUrl(logoUrl);
        return companyProfileRepository.save(profile);
    }

    public Map<String, Object> getDashboardStats(Authentication auth) {
        CompanyProfile company = getProfile(auth);
        List<Internship> internships = internshipRepository.findByCompanyId(company.getId());
        List<Application> allApps = applicationRepository.findByInternship_Company_Id(company.getId());

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalInternships", internships.size());
        stats.put("activeInternships", internships.stream().filter(i -> i.getStatus() == InternshipStatus.PUBLISHED).count());
        stats.put("totalApplications", allApps.size());
        stats.put("shortlisted", allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count());
        stats.put("selected", allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.SELECTED).count());
        stats.put("rejected", allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count());
        stats.put("verificationStatus", company.getVerificationStatus());
        stats.put("companyName", company.getCompanyName());
        return stats;
    }

    public List<Internship> getMyInternships(Authentication auth) {
        CompanyProfile company = getProfile(auth);
        return internshipRepository.findByCompanyId(company.getId());
    }

    @Transactional
    public Internship createInternship(Authentication auth, Internship internship) {
        CompanyProfile company = getProfile(auth);
        if (!"VERIFIED".equals(company.getVerificationStatus())) {
            throw new IllegalArgumentException("Your company must be verified before posting internships");
        }
        internship.setCompany(company);
        internship.setStatus(InternshipStatus.DRAFT);
        return internshipRepository.save(internship);
    }

    @Transactional
    public Internship updateInternship(Authentication auth, Long id, Internship updated) {
        Internship internship = getOwnedInternship(auth, id);
        if (updated.getTitle() != null) internship.setTitle(updated.getTitle());
        if (updated.getDescription() != null) internship.setDescription(updated.getDescription());
        if (updated.getLocation() != null) internship.setLocation(updated.getLocation());
        if (updated.getStipend() != null) internship.setStipend(updated.getStipend());
        if (updated.getRequiredSkills() != null) internship.setRequiredSkills(updated.getRequiredSkills());
        if (updated.getDeadline() != null) internship.setDeadline(updated.getDeadline());
        return internshipRepository.save(internship);
    }

    @Transactional
    public Internship publishInternship(Authentication auth, Long id) {
        CompanyProfile company = getProfile(auth);
        if (!"VERIFIED".equals(company.getVerificationStatus())) {
            throw new IllegalArgumentException("Company must be verified to publish");
        }
        Internship internship = getOwnedInternship(auth, id);
        internship.setStatus(InternshipStatus.PUBLISHED);
        return internshipRepository.save(internship);
    }

    @Transactional
    public Internship closeInternship(Authentication auth, Long id) {
        Internship internship = getOwnedInternship(auth, id);
        internship.setStatus(InternshipStatus.CLOSED);
        return internshipRepository.save(internship);
    }

    @Transactional
    public void deleteInternship(Authentication auth, Long id) {
        Internship internship = getOwnedInternship(auth, id);
        if (internship.getStatus() == InternshipStatus.PUBLISHED) {
            throw new IllegalArgumentException("Cannot delete a published internship. Close it first.");
        }
        internshipRepository.delete(internship);
    }

    public List<Application> getApplicants(Authentication auth, Long internshipId) {
        Internship internship = getOwnedInternship(auth, internshipId);
        return applicationRepository.findByInternship(internship);
    }

    @Transactional
    public Application updateApplicationStatus(Authentication auth, Long applicationId, String status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Ownership check
        CompanyProfile company = getProfile(auth);
        if (!application.getInternship().getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Access denied");
        }

        ApplicationStatus newStatus = ApplicationStatus.valueOf(status.toUpperCase());
        application.setStatus(newStatus);
        Application saved = applicationRepository.save(application);

        // Notify the student
        String msg = "Your application for '" + application.getInternship().getTitle() + "' has been " + status.toLowerCase();
        createNotification(application.getStudent().getUser(), msg, "APPLICATION_STATUS");

        return saved;
    }

    private Internship getOwnedInternship(Authentication auth, Long id) {
        CompanyProfile company = getProfile(auth);
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Internship not found"));
        if (!internship.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Access denied: you do not own this internship");
        }
        return internship;
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
