package com.edublitz.userservice.service;

import com.edublitz.userservice.model.AuditLog;
import com.edublitz.userservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Asynchronous audit logging service.
 * All writes are fire-and-forget to not block the main request thread.
 * Compliant with medical audit trail requirements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String userId, String userEmail, String userRole,
                    String action, String resourceType, String resourceId,
                    String details, String ipAddress, boolean success) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .timestamp(Instant.now())
                    .success(success)
                    .build();

            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Audit failures must never crash the main flow; log and continue
            log.error("Failed to write audit log for action={} user={}: {}", action, userEmail, e.getMessage());
        }
    }
}
