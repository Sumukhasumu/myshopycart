package com.myshopycart.authservice.controller;

import com.myshopycart.authservice.dto.LoginRequest;
import com.myshopycart.authservice.dto.LoginResponse;
import com.myshopycart.authservice.dto.RegisterRequest;
import com.myshopycart.authservice.dto.RegisterResponse;
import com.myshopycart.authservice.entity.User;
import com.myshopycart.authservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        System.out.println(
                "LOGIN CONTROLLER CALLED: username="
                        + request.getUsername());

        User user = authService.login(request);

        String token = authService.generateToken(user);

        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                token
        );

        return ResponseEntity.ok(response);
    }
}