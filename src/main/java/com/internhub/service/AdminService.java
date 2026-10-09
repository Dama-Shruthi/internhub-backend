package com.internhub.service;

import com.internhub.entity.*;
import com.internhub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final InternshipRepository internshipRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationRepository notificationRepository;
    private final StudentProfileRepository studentProfileRepository;

    public AdminService(UserRepository userRepository,
                        CompanyProfileRepository companyProfileRepository,
                        InternshipRepository internshipRepository,
                        ApplicationRepository applicationRepository,
                        NotificationRepository notificationRepository,
                        StudentProfileRepository studentProfileRepository) {
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.internshipRepository = internshipRepository;
        this.applicationRepository = applicationRepository;
        this.notificationRepository = notificationRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalStudents", studentProfileRepository.count());
        stats.put("totalCompanies", companyProfileRepository.count());
        stats.put("pendingCompanies", companyProfileRepository.countByVerificationStatus("PENDING"));
        stats.put("verifiedCompanies", companyProfileRepository.countByVerificationStatus("VERIFIED"));
        stats.put("totalInternships", internshipRepository.count());
        stats.put("publishedInternships", internshipRepository.countByStatus(InternshipStatus.PUBLISHED));
        stats.put("totalApplications", applicationRepository.count());
        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    public List<CompanyProfile> getAllCompanies() {
        return companyProfileRepository.findAll();
    }

    @Transactional
    public CompanyProfile verifyCompany(Long companyId) {
        CompanyProfile company = companyProfileRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        company.setVerificationStatus("VERIFIED");
        companyProfileRepository.save(company);

        // Notify the company user
        Notification n = new Notification();
        n.setUser(company.getUser());
        n.setMessage("Congratulations! Your company '" + company.getCompanyName() + "' has been verified. You can now post internships.");
        n.setType("COMPANY_VERIFIED");
        notificationRepository.save(n);

        return company;
    }

    @Transactional
    public CompanyProfile rejectCompany(Long companyId) {
        CompanyProfile company = companyProfileRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        company.setVerificationStatus("REJECTED");
        companyProfileRepository.save(company);

        Notification n = new Notification();
        n.setUser(company.getUser());
        n.setMessage("Your company profile has been rejected. Please update your details and reapply.");
        n.setType("COMPANY_REJECTED");
        notificationRepository.save(n);
        return company;
    }

    @Transactional
    public CompanyProfile suspendCompany(Long companyId) {
        CompanyProfile company = companyProfileRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        company.setVerificationStatus("SUSPENDED");
        company.getUser().setActive(false);
        userRepository.save(company.getUser());
        return companyProfileRepository.save(company);
    }

    public List<Internship> getAllInternships() {
        return internshipRepository.findAll();
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Transactional
    public void deleteInternship(Long id) {
        internshipRepository.deleteById(id);
    }
}
