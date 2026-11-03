package com.myshopycart.authservice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    @Test
    void shouldStoreMobileNumber() {

        User user = new User(
                "sumu",
                "sumu@example.com",
                "1234567890",
                "password",
                "USER");

        assertEquals(
                "1234567890",
                user.getMobileNumber());
    }

    @Test
    void shouldUpdateMobileNumber() {

        User user = new User(
                "sumu",
                "sumu@example.com",
                "password",
                "USER");

        assertNull(user.getMobileNumber());

        user.setMobileNumber("1234567890");

        assertEquals(
                "1234567890",
                user.getMobileNumber());
    }

    @Test
    void existingConstructorShouldRemainCompatible() {

        User user = new User(
                "sumu",
                "sumu@example.com",
                "password",
                "USER");

        assertEquals("sumu", user.getUsername());
        assertEquals(
                "sumu@example.com",
                user.getEmail());
        assertEquals(
                "password",
                user.getPassword());
        assertEquals(
                "USER",
                user.getRole());
        assertNull(user.getMobileNumber());
    }
}