package com.myshopycart.authservice.service;

import com.myshopycart.authservice.dto.LoginRequest;
import com.myshopycart.authservice.dto.RegisterRequest;
import com.myshopycart.authservice.entity.User;
import com.myshopycart.authservice.exception.DuplicateRegistrationException;
import com.myshopycart.authservice.repository.UserRepository;
import com.myshopycart.authservice.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {

        // Check for duplicate username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateRegistrationException(
                    "This username is already registered.");
        }

        // Check for duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateRegistrationException(
                    "This email is already registered.");
        }

        // Check for duplicate mobile number when provided
        if (request.getMobileNumber() != null
                && !request.getMobileNumber().isBlank()
                && userRepository.findByMobileNumber(
                        request.getMobileNumber()).isPresent()) {

            throw new DuplicateRegistrationException(
                    "This mobile number is already registered.");
        }

        // Create a new user
        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());

        // Store the encoded password, never the raw password
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole("USER");

        // Save and return the registered user
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid username or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid username or password");
        }

        return user;
    }

    public String generateToken(User user) {

        return jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
}