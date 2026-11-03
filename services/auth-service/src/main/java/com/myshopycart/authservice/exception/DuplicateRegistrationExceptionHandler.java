package com.myshopycart.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class DuplicateRegistrationExceptionHandler {

    @ExceptionHandler(DuplicateRegistrationException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateRegistration(
            DuplicateRegistrationException exception) {

        Map<String, String> response = Map.of(
                "message", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}