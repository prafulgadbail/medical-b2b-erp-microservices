package com.edublitz.userservice.repository;

import com.edublitz.userservice.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    Page<AuditLog> findByUserId(String userId, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(Instant from, Instant to, Pageable pageable);

    Page<AuditLog> findByUserIdAndTimestampBetween(String userId, Instant from, Instant to, Pageable pageable);
}
