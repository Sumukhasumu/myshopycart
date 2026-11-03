package com.myshopycart.authservice.dto;

public class RegisterResponse {

    private Long id;
    private String username;
    private String email;
    private String mobileNumber;
    private String role;

    public RegisterResponse() {
    }

    public RegisterResponse(
            Long id,
            String username,
            String email,
            String mobileNumber,
            String role) {

        this.id = id;
        this.username = username;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getRole() {
        return role;
    }
}