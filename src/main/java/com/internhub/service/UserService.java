package com.internhub.service;

import com.internhub.dto.UserRegistrationDto;
import com.internhub.entity.CompanyProfile;
import com.internhub.entity.Role;
import com.internhub.entity.StudentProfile;
import com.internhub.entity.User;
import com.internhub.repository.CompanyProfileRepository;
import com.internhub.repository.StudentProfileRepository;
import com.internhub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, 
                       StudentProfileRepository studentProfileRepository,
                       CompanyProfileRepository companyProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(registrationDto.getRole());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // Initialize associated profile based on role
        if (savedUser.getRole() == Role.STUDENT) {
            StudentProfile profile = new StudentProfile();
            profile.setUser(savedUser);
            studentProfileRepository.save(profile);
        } else if (savedUser.getRole() == Role.COMPANY) {
            CompanyProfile profile = new CompanyProfile();
            profile.setUser(savedUser);
            profile.setVerificationStatus("PENDING"); // New companies are pending verification
            companyProfileRepository.save(profile);
        }
        
        return savedUser;
    }
}
