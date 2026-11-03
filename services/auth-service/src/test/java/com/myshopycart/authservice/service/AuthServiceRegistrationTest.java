package com.myshopycart.authservice.service;

import com.myshopycart.authservice.dto.RegisterRequest;
import com.myshopycart.authservice.entity.User;
import com.myshopycart.authservice.exception.DuplicateRegistrationException;
import com.myshopycart.authservice.repository.UserRepository;
import com.myshopycart.authservice.security.JwtService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegistrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserWithMobileNumber() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("sumu");
        request.setEmail("sumu@example.com");
        request.setMobileNumber("1234567890");
        request.setPassword("password");

        when(userRepository.findByUsername("sumu"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("sumu@example.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findByMobileNumber("1234567890"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password"))
                .thenReturn("encoded-password");

        User savedUser = new User(
                "sumu",
                "sumu@example.com",
                "1234567890",
                "encoded-password",
                "USER");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = authService.register(request);

        assertEquals("sumu", result.getUsername());
        assertEquals("sumu@example.com", result.getEmail());
        assertEquals("1234567890", result.getMobileNumber());
        assertEquals("encoded-password", result.getPassword());
        assertEquals("USER", result.getRole());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateUsername() {

        RegisterRequest request = createRequest();

        User existingUser = new User(
                "sumu",
                "existing@example.com",
                "1111111111",
                "encoded-password",
                "USER");

        when(userRepository.findByUsername("sumu"))
                .thenReturn(Optional.of(existingUser));

        DuplicateRegistrationException exception = assertThrows(
                DuplicateRegistrationException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "This username is already registered.",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void shouldRejectDuplicateEmail() {

        RegisterRequest request = createRequest();

        User existingUser = new User(
                "existing",
                "sumu@example.com",
                "1111111111",
                "encoded-password",
                "USER");

        when(userRepository.findByUsername("sumu"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("sumu@example.com"))
                .thenReturn(Optional.of(existingUser));

        DuplicateRegistrationException exception = assertThrows(
                DuplicateRegistrationException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "This email is already registered.",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).findByMobileNumber(any());
    }

    @Test
    void shouldRejectDuplicateMobileNumber() {

        RegisterRequest request = createRequest();

        User existingUser = new User(
                "existing",
                "existing@example.com",
                "1234567890",
                "encoded-password",
                "USER");

        when(userRepository.findByUsername("sumu"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("sumu@example.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findByMobileNumber("1234567890"))
                .thenReturn(Optional.of(existingUser));

        DuplicateRegistrationException exception = assertThrows(
                DuplicateRegistrationException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "This mobile number is already registered.",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    private RegisterRequest createRequest() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("sumu");
        request.setEmail("sumu@example.com");
        request.setMobileNumber("1234567890");
        request.setPassword("password");

        return request;
    }
}