package com.edublitz.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Entry point for the User Service.
 * Handles authentication, JWT issuance, role-based access control,
 * and organization management for the Medical B2B ERP platform.
 */
@SpringBootApplication
@EnableMongoAuditing
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
