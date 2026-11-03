package com.myshopycart.authservice.dto;

public class CustomerContactResponse {

    private Long userId;
    private String username;
    private String email;
    private String mobileNumber;

    public CustomerContactResponse() {
    }

    public CustomerContactResponse(
            Long userId,
            String username,
            String email,
            String mobileNumber) {

        this.userId = userId;
        this.username = username;
        this.email = email;
        this.mobileNumber = mobileNumber;
    }

    public Long getUserId() {
        return userId;
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
}