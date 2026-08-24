package com.lastmile.delivery.service;

import com.lastmile.delivery.dto.*;
import com.lastmile.delivery.entity.Role;
import com.lastmile.delivery.entity.User;
import com.lastmile.delivery.exception.BadRequestException;
import com.lastmile.delivery.exception.ResourceNotFoundException;
import com.lastmile.delivery.repository.UserRepository;
import com.lastmile.delivery.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .build();

        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .user(convertToResponse(user))
                .token(token)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .user(convertToResponse(user))
                .token(token)
                .build();
    }

    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToResponse(user);
    }

    public UserResponse updateProfile(String userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }

        user = userRepository.save(user);
        return convertToResponse(user);
    }

    public List<UserResponse> searchCustomers(String query) {
        List<User> customers;
        if (query != null && !query.trim().isEmpty()) {
            customers = userRepository.searchUsersByRoleAndQuery(Role.CUSTOMER, query.trim());
        } else {
            customers = userRepository.findByRole(Role.CUSTOMER);
        }

        return customers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> createGuestCustomer(RegisterRequest request) {
        Optional<User> existingOpt = userRepository.findByEmail(request.getEmail());
        Map<String, Object> result = new HashMap<>();

        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();
            result.put("user", convertToResponse(existing));
            result.put("isExisting", true);
            result.put("message", "Existing customer found and selected");
            return result;
        }

        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "Customer123!";
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(Role.CUSTOMER)
                .build();

        user = userRepository.save(user);

        result.put("user", convertToResponse(user));
        result.put("tempPassword", rawPassword);
        result.put("isExisting", false);
        return result;
    }

    public UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}
