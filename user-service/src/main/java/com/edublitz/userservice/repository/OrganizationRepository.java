package com.edublitz.userservice.repository;

import com.edublitz.userservice.model.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends MongoRepository<Organization, String> {

    Optional<Organization> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Organization> findByType(Organization.OrganizationType type);

    List<Organization> findByActiveTrue();
}
