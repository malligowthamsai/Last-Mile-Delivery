package com.lastmile.delivery.service;

import com.lastmile.delivery.dto.AgentCreateRequest;
import com.lastmile.delivery.dto.AgentUpdateRequest;
import com.lastmile.delivery.entity.AgentProfile;
import com.lastmile.delivery.entity.Role;
import com.lastmile.delivery.entity.User;
import com.lastmile.delivery.entity.Zone;
import com.lastmile.delivery.exception.BadRequestException;
import com.lastmile.delivery.exception.ResourceNotFoundException;
import com.lastmile.delivery.repository.AgentProfileRepository;
import com.lastmile.delivery.repository.UserRepository;
import com.lastmile.delivery.repository.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AgentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgentProfileRepository agentProfileRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Map<String, Object>> getAllAgents() {
        List<User> agentUsers = userRepository.findByRole(Role.AGENT);
        List<Map<String, Object>> result = new ArrayList<>();

        for (User u : agentUsers) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("email", u.getEmail());
            map.put("phone", u.getPhone());
            map.put("role", u.getRole());

            AgentProfile profile = agentProfileRepository.findByUserId(u.getId()).orElse(null);
            map.put("agentProfile", profile);
            result.add(map);
        }
        return result;
    }

    @Transactional
    public Map<String, Object> createAgent(AgentCreateRequest request) {
        if (userRepository.findByEmail(request.getEmail().trim()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new BadRequestException("Zone not found"));

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.AGENT)
                .build();

        user = userRepository.save(user);

        AgentProfile profile = AgentProfile.builder()
                .user(user)
                .zone(zone)
                .isAvailable(true)
                .build();

        profile = agentProfileRepository.save(profile);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("role", user.getRole());
        result.put("agentProfile", profile);
        return result;
    }

    @Transactional
    public AgentProfile updateAgent(String userId, AgentUpdateRequest request) {
        AgentProfile profile = agentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent profile not found"));

        if (request.getZoneId() != null && !request.getZoneId().trim().isEmpty()) {
            Zone zone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new BadRequestException("Zone not found"));
            profile.setZone(zone);
        }

        if (request.getIsAvailable() != null) {
            profile.setIsAvailable(request.getIsAvailable());
        }

        return agentProfileRepository.save(profile);
    }

    @Transactional
    public Map<String, Object> toggleAvailability(String userId, Boolean isAvailable) {
        AgentProfile profile = agentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent profile not found"));

        profile.setIsAvailable(isAvailable);
        profile = agentProfileRepository.save(profile);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Availability set to " + isAvailable);
        result.put("profile", profile);
        return result;
    }

    public Map<String, Object> getAgentMe(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AgentProfile profile = agentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent profile not found"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", user);
        result.put("profile", profile);
        return result;
    }
}
