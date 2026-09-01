package com.edublitz.userservice.controller;

import com.edublitz.userservice.dto.UserResponse;
import com.edublitz.userservice.exception.ResourceNotFoundException;
import com.edublitz.userservice.model.User;
import com.edublitz.userservice.repository.UserRepository;
import com.edublitz.userservice.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and management operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(mapToResponse(user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (Admin only)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return ResponseEntity.ok(mapToResponse(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users with pagination (Admin only)")
    public ResponseEntity<Page<UserResponse>> listUsers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userRepository.findAll(pageable).map(this::mapToResponse));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a user account (Admin only)")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails adminDetails,
            HttpServletRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setActive(false);
        userRepository.save(user);

        User admin = userRepository.findByEmail(adminDetails.getUsername()).orElse(null);
        String adminId = admin != null ? admin.getId() : "unknown";
        auditService.log(adminId, adminDetails.getUsername(), "ADMIN",
                "DEACTIVATE_USER", "USER", id,
                "User deactivated by admin", request.getRemoteAddr(), true);

        return ResponseEntity.noContent().build();
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .organizationId(user.getOrganizationId())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
