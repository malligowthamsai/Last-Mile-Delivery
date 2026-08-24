package com.lastmile.delivery.service;

import com.lastmile.delivery.entity.*;
import com.lastmile.delivery.exception.BadRequestException;
import com.lastmile.delivery.exception.ResourceNotFoundException;
import com.lastmile.delivery.repository.AgentProfileRepository;
import com.lastmile.delivery.repository.OrderRepository;
import com.lastmile.delivery.repository.TrackingHistoryRepository;
import com.lastmile.delivery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AgentAssignmentService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AgentProfileRepository agentProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrackingHistoryRepository trackingHistoryRepository;

    @Transactional
    public Map<String, Object> autoAssign(String orderId, String pickupZoneId, String actorId, Role actorRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Step 1: Look for available agent in pickup zone
        AgentProfile profile = null;
        if (pickupZoneId != null) {
            profile = agentProfileRepository.findFirstByZoneIdAndIsAvailableTrue(pickupZoneId).orElse(null);
        }

        // Step 2: Fallback to any available agent
        if (profile == null) {
            profile = agentProfileRepository.findFirstByIsAvailableTrue()
                    .orElseThrow(() -> new BadRequestException(
                            "No available delivery agents at this time. Please try manual assignment or retry later."));
        }

        User agentUser = profile.getUser();
        User actor = userRepository.findById(actorId).orElse(null);

        // Step 3: Assign and set agent unavailable
        order.setAgent(agentUser);
        order.setStatus(OrderStatus.AGENT_ASSIGNED);
        orderRepository.save(order);

        profile.setIsAvailable(false);
        agentProfileRepository.save(profile);

        boolean zoneMatch = profile.getZone() != null && profile.getZone().getId().equals(pickupZoneId);
        String note = String.format("Auto-assigned to agent: %s (Zone: %s)",
                agentUser.getName(),
                zoneMatch ? "pickup zone match" : "fallback — any available");

        TrackingHistory history = TrackingHistory.builder()
                .order(order)
                .status(OrderStatus.AGENT_ASSIGNED)
                .changedBy(actor)
                .changedByRole(actorRole)
                .note(note)
                .build();
        trackingHistoryRepository.save(history);

        Map<String, Object> result = new HashMap<>();
        result.put("agentId", agentUser.getId());
        result.put("agentName", agentUser.getName());
        return result;
    }

    @Transactional
    public Map<String, Object> manualAssign(String orderId, String targetAgentId, String actorId, Role actorRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        AgentProfile profile = agentProfileRepository.findByUserId(targetAgentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent profile not found"));

        User agentUser = profile.getUser();
        User actor = userRepository.findById(actorId).orElse(null);

        order.setAgent(agentUser);
        order.setStatus(OrderStatus.AGENT_ASSIGNED);
        orderRepository.save(order);

        profile.setIsAvailable(false);
        agentProfileRepository.save(profile);

        TrackingHistory history = TrackingHistory.builder()
                .order(order)
                .status(OrderStatus.AGENT_ASSIGNED)
                .changedBy(actor)
                .changedByRole(actorRole)
                .note("Manually assigned to agent: " + agentUser.getName())
                .build();
        trackingHistoryRepository.save(history);

        Map<String, Object> result = new HashMap<>();
        result.put("agentId", agentUser.getId());
        result.put("agentName", agentUser.getName());
        return result;
    }
}
