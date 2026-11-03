package com.myshopycart.authservice.controller;

import com.myshopycart.authservice.dto.CustomerContactResponse;
import com.myshopycart.authservice.entity.User;
import com.myshopycart.authservice.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/users")
public class CustomerContactController {

    private final UserRepository userRepository;

    public CustomerContactController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}/contact")
    public ResponseEntity<CustomerContactResponse> getCustomerContact(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        CustomerContactResponse response =
                new CustomerContactResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getMobileNumber()
                );

        return ResponseEntity.ok(response);
    }
}