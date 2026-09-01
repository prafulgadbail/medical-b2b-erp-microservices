package com.edublitz.userservice.controller;

import com.edublitz.userservice.exception.BadRequestException;
import com.edublitz.userservice.exception.ResourceNotFoundException;
import com.edublitz.userservice.model.Organization;
import com.edublitz.userservice.repository.OrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization management for hospitals, distributors, and vendors")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {

    private final OrganizationRepository organizationRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new organization (Admin only)")
    public ResponseEntity<Organization> create(@Valid @RequestBody Organization org) {
        if (organizationRepository.existsByRegistrationNumber(org.getRegistrationNumber())) {
            throw new BadRequestException("Registration number already exists: " + org.getRegistrationNumber());
        }
        org.setActive(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationRepository.save(org));
    }

    @GetMapping
    @Operation(summary = "List all organizations")
    public ResponseEntity<List<Organization>> listAll() {
        return ResponseEntity.ok(organizationRepository.findByActiveTrue());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<Organization> getById(@PathVariable String id) {
        return ResponseEntity.ok(organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id)));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get organizations by type")
    public ResponseEntity<List<Organization>> getByType(@PathVariable Organization.OrganizationType type) {
        return ResponseEntity.ok(organizationRepository.findByType(type));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update organization (Admin only)")
    public ResponseEntity<Organization> update(@PathVariable String id,
                                               @Valid @RequestBody Organization updated) {
        Organization existing = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id));
        updated.setId(existing.getId());
        updated.setCreatedAt(existing.getCreatedAt());
        return ResponseEntity.ok(organizationRepository.save(updated));
    }
}
