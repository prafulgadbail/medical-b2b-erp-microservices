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

/**
 * Represents a medical organization (Hospital, Distributor, or Vendor).
 *
 * MongoDB Collection: organizations
 * Indexes: registrationNumber (unique), type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "organizations")
public class Organization {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String registrationNumber;   // Medical license / GST / NPI

    private OrganizationType type;

    private Address address;

    private String contactEmail;
    private String contactPhone;

    private boolean active;

    private String licenseNumber;        // Drug license for distributors
    private Instant licenseExpiry;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum OrganizationType {
        HOSPITAL, DISTRIBUTOR, VENDOR
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String pincode;
        private String country;
    }
}
