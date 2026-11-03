package com.myshopycart.authservice.controller;

import com.myshopycart.authservice.dto.UserAddressRequest;
import com.myshopycart.authservice.dto.UserAddressResponse;
import com.myshopycart.authservice.entity.User;
import com.myshopycart.authservice.service.UserAddressService;
import com.myshopycart.authservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;
    private final UserRepository userRepository;

    public UserAddressController(
            UserAddressService userAddressService,
            UserRepository userRepository) {

        this.userAddressService = userAddressService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<UserAddressResponse> createAddress(
            @RequestBody UserAddressRequest request,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        UserAddressResponse response =
                userAddressService.createAddress(
                        userId,
                        request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> getAddresses(
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                userAddressService.getAddresses(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAddressResponse> getAddress(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                userAddressService.getAddress(
                        userId,
                        id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAddressResponse> updateAddress(
            @PathVariable Long id,
            @RequestBody UserAddressRequest request,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                userAddressService.updateAddress(
                        userId,
                        id,
                        request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);

        userAddressService.deleteAddress(
                userId,
                id);

        return ResponseEntity.noContent().build();
    }

    private Long getAuthenticatedUserId(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "User is not authenticated");
        }

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"));

        return user.getId();
    }
}