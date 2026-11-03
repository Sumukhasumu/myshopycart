package com.myshopycart.authservice.config;

import com.myshopycart.authservice.entity.User;
import com.myshopycart.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String username = "admin";
        String email = "admin@myshopycart.com";
        String password = "Admin@123";

        User admin = userRepository.findByUsername(username)
                .orElseGet(User::new);

        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(
                passwordEncoder.encode(password));
        admin.setRole("ADMIN");

        userRepository.save(admin);

        System.out.println(
                "MYSHOPYCART ADMIN user initialized: "
                        + username);
    }
}