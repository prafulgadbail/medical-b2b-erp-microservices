package com.edublitz.userservice.controller;

import com.edublitz.userservice.dto.AuthRequest;
import com.edublitz.userservice.dto.AuthResponse;
import com.edublitz.userservice.dto.RegisterRequest;
import com.edublitz.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT login, registration, and token refresh")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request, ip));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT tokens")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(authService.login(request, ip));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
