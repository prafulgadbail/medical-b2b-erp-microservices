package com.edublitz.userservice.repository;

import com.edublitz.userservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByOrganizationId(String organizationId);

    List<User> findByRole(User.Role role);

    Optional<User> findByEmailAndActive(String email, boolean active);

    long countByOrganizationId(String organizationId);
}
