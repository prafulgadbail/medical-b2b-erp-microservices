package com.edublitz.userservice.dto;

import com.edublitz.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private User.Role role;
    private String organizationId;
    private boolean active;
    private Instant createdAt;
    private Instant lastLoginAt;
}
