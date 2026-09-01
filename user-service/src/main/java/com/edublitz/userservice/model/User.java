package com.edublitz.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

/**
 * Represents a user in the Medical B2B ERP system.
 * Each user belongs to exactly one organization and carries a single role.
 *
 * MongoDB Collection: users
 * Indexes: email (unique), organizationId
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;    // BCrypt-hashed, never returned in API responses

    private String firstName;
    private String lastName;
    private String phone;

    private Role role;

    @Indexed
    private String organizationId;

    private boolean active;

    @Builder.Default
    private boolean emailVerified = false;

    private String refreshToken;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant lastLoginAt;

    public enum Role {
        ADMIN, DISTRIBUTOR, HOSPITAL
    }
}
