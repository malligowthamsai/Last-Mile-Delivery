package com.lastmile.delivery.controller;

import com.lastmile.delivery.dto.*;
import com.lastmile.delivery.entity.Order;
import com.lastmile.delivery.entity.OrderStatus;
import com.lastmile.delivery.entity.Role;
import com.lastmile.delivery.security.UserPrincipal;
import com.lastmile.delivery.service.OrderService;
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
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateCharge(@Valid @RequestBody OrderCalculateRequest request) {
        OrderCalculateResponse breakdown = orderService.calculateCharge(request);
        return ResponseEntity.ok(Collections.singletonMap("breakdown", breakdown));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody OrderCreateRequest request) {
        Role role = userPrincipal.getRole();
        Map<String, Object> response = orderService.createOrder(request, userPrincipal.getId(), role);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Order>> listOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String zoneId,
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String customerId) {
        Role role = userPrincipal.getRole();
        OrderStatus orderStatus = (status != null && !status.trim().isEmpty())
                ? OrderStatus.valueOf(status.trim().toUpperCase())
                : null;
        List<Order> orders = orderService.listOrders(orderStatus, zoneId, agentId, customerId, userPrincipal.getId(), role);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        Role role = userPrincipal.getRole();
        Map<String, Object> orderDetails = orderService.getOrderDetails(id, userPrincipal.getId(), role);
        return ResponseEntity.ok(orderDetails);
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<Map<String, Object>> getPublicTracking(@PathVariable String id) {
        Map<String, Object> trackingInfo = orderService.getPublicTracking(id);
        return ResponseEntity.ok(trackingInfo);
    }

    @PostMapping("/{id}/auto-assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> autoAssign(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        Role role = userPrincipal.getRole();
        Map<String, Object> result = orderService.autoAssignAgent(id, userPrincipal.getId(), role);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> manualAssign(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String agentId = body.get("agentId");
        Role role = userPrincipal.getRole();
        Map<String, Object> result = orderService.manualAssignAgent(id, agentId, userPrincipal.getId(), role);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequest request) {
        Role role = userPrincipal.getRole();
        Map<String, Object> result = orderService.updateOrderStatus(id, request, userPrincipal.getId(), role);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> reschedule(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody RescheduleRequestDto request) {
        Role role = userPrincipal.getRole();
        Map<String, Object> result = orderService.rescheduleOrder(id, request, userPrincipal.getId(), role);
        return ResponseEntity.ok(result);
    }
}
