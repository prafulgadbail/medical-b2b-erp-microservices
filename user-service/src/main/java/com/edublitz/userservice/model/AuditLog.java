package com.edublitz.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Immutable audit log for all critical actions in the User Service.
 * Required for medical domain compliance (21 CFR Part 11 / HIPAA).
 *
 * MongoDB Collection: audit_logs
 * Indexes: userId, action, timestamp (TTL: 7 years per HIPAA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String userEmail;
    private String userRole;

    @Indexed
    private String action;          // LOGIN, LOGOUT, CREATE_USER, UPDATE_ROLE, etc.

    private String resourceType;    // USER, ORGANIZATION
    private String resourceId;

    private String details;         // JSON string of changed fields

    private String ipAddress;
    private String userAgent;

    @Indexed
    private Instant timestamp;

    private boolean success;
    private String errorMessage;
}
