package com.lastmile.delivery.controller;

import com.lastmile.delivery.dto.*;
import com.lastmile.delivery.security.UserPrincipal;
import com.lastmile.delivery.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse user = authService.getUserProfile(userPrincipal.getId());
        return ResponseEntity.ok(Collections.singletonMap("user", user));
    }

    @PatchMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ProfileUpdateRequest request) {
        UserResponse updated = authService.updateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(Map.of("user", updated, "message", "Profile updated successfully"));
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> listCustomers(@RequestParam(value = "q", required = false) String q) {
        List<UserResponse> customers = authService.searchCustomers(q);
        return ResponseEntity.ok(customers);
    }

    @PostMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createGuestCustomer(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = authService.createGuestCustomer(request);
        boolean isExisting = Boolean.TRUE.equals(result.get("isExisting"));
        return new ResponseEntity<>(result, isExisting ? HttpStatus.OK : HttpStatus.CREATED);
    }
}
