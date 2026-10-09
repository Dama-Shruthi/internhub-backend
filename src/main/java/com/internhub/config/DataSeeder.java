package com.internhub.config;

import com.internhub.entity.Role;
import com.internhub.entity.User;
import com.internhub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Automatically create the Admin user if it doesn't exist
        if (userRepository.findByEmail("admin@internhub.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@internhub.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("✅ ADMIN USER SEEDED: admin@internhub.com / admin123");
        }
    }
}
