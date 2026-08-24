package com.lastmile.delivery.controller;

import com.lastmile.delivery.dto.AgentAvailabilityRequest;
import com.lastmile.delivery.dto.AgentCreateRequest;
import com.lastmile.delivery.dto.AgentUpdateRequest;
import com.lastmile.delivery.entity.AgentProfile;
import com.lastmile.delivery.security.UserPrincipal;
import com.lastmile.delivery.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createAgent(@Valid @RequestBody AgentCreateRequest request) {
        Map<String, Object> agent = agentService.createAgent(request);
        return new ResponseEntity<>(agent, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentProfile> updateAgent(
            @PathVariable String id,
            @RequestBody AgentUpdateRequest request) {
        AgentProfile updated = agentService.updateAgent(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/availability")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<Map<String, Object>> toggleAvailability(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AgentAvailabilityRequest request) {
        Map<String, Object> result = agentService.toggleAvailability(userPrincipal.getId(), request.getIsAvailable());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<Map<String, Object>> getAgentMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> result = agentService.getAgentMe(userPrincipal.getId());
        return ResponseEntity.ok(result);
    }
}
