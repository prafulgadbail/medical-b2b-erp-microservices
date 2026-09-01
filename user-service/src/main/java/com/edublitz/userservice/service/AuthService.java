package com.edublitz.userservice.service;

import com.edublitz.userservice.dto.*;
import com.edublitz.userservice.exception.BadRequestException;
import com.edublitz.userservice.exception.ResourceNotFoundException;
import com.edublitz.userservice.model.Organization;
import com.edublitz.userservice.model.User;
import com.edublitz.userservice.repository.OrganizationRepository;
import com.edublitz.userservice.repository.UserRepository;
import com.edublitz.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final AuditService auditService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + request.getOrganizationId()));

        if (!org.isActive()) {
            throw new BadRequestException("Organization is not active");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .organizationId(org.getId())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {} with role {}", savedUser.getEmail(), savedUser.getRole());

        auditService.log(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name(),
                "REGISTER", "USER", savedUser.getId(), "User registered", ipAddress, true);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtService.generateToken(userDetails, savedUser.getId(),
                savedUser.getRole().name(), savedUser.getOrganizationId());
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        savedUser.setRefreshToken(passwordEncoder.encode(refreshToken));
        userRepository.save(savedUser);

        return buildAuthResponse(accessToken, refreshToken, savedUser);
    }

    public AuthResponse login(AuthRequest request, String ipAddress) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null) {
                auditService.log(user.getId(), user.getEmail(), user.getRole().name(),
                        "LOGIN_FAILED", "USER", user.getId(), "Bad credentials", ipAddress, false);
            }
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails, user.getId(),
                user.getRole().name(), user.getOrganizationId());
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(passwordEncoder.encode(refreshToken));
        userRepository.save(user);

        auditService.log(user.getId(), user.getEmail(), user.getRole().name(),
                "LOGIN", "USER", user.getId(), "Successful login", ipAddress, true);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        UserResponse userResponse = UserResponse.builder()
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

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .user(userResponse)
                .build();
    }
}
