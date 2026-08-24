package com.lastmile.delivery.service;

import com.lastmile.delivery.dto.*;
import com.lastmile.delivery.entity.*;
import com.lastmile.delivery.exception.BadRequestException;
import com.lastmile.delivery.exception.ResourceNotFoundException;
import com.lastmile.delivery.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private TrackingHistoryRepository trackingHistoryRepository;

    @Autowired
    private RescheduleRequestRepository rescheduleRequestRepository;

    @Autowired
    private AgentProfileRepository agentProfileRepository;

    @Autowired
    private RateEngineService rateEngineService;

    @Autowired
    private AgentAssignmentService agentAssignmentService;

    @Autowired
    private NotificationService notificationService;

    public OrderCalculateResponse calculateCharge(OrderCalculateRequest request) {
        return rateEngineService.calculateCharge(request);
    }

    @Transactional
    public Map<String, Object> createOrder(OrderCreateRequest request, String authenticatedUserId, Role authenticatedRole) {
        String targetCustomerId = authenticatedUserId;
        User createdBy = null;

        if (request.getCustomerId() != null && !request.getCustomerId().trim().isEmpty()) {
            if (authenticatedRole != Role.ADMIN) {
                throw new BadRequestException("Only admin can create orders on behalf of customers");
            }
            targetCustomerId = request.getCustomerId().trim();
            createdBy = userRepository.findById(authenticatedUserId).orElse(null);
        }

        User customer = userRepository.findById(targetCustomerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        OrderCalculateRequest calcRequest = new OrderCalculateRequest();
        calcRequest.setPickupPincode(request.getPickupPincode());
        calcRequest.setDropPincode(request.getDropPincode());
        calcRequest.setLength(request.getLength());
        calcRequest.setBreadth(request.getBreadth());
        calcRequest.setHeight(request.getHeight());
        calcRequest.setActualWeight(request.getActualWeight());
        calcRequest.setOrderType(request.getOrderType());
        calcRequest.setPaymentType(request.getPaymentType());
        calcRequest.setRateType(request.getRateType());

        OrderCalculateResponse breakdown = rateEngineService.calculateCharge(calcRequest);

        Zone pickupZone = zoneRepository.findById(breakdown.getPickupZone().getZoneId()).orElse(null);
        Zone dropZone = zoneRepository.findById(breakdown.getDropZone().getZoneId()).orElse(null);

        Order order = Order.builder()
                .customer(customer)
                .createdBy(createdBy)
                .pickupAddress(request.getPickupAddress())
                .pickupPincode(request.getPickupPincode())
                .dropAddress(request.getDropAddress())
                .dropPincode(request.getDropPincode())
                .pickupZone(pickupZone)
                .dropZone(dropZone)
                .length(request.getLength())
                .breadth(request.getBreadth())
                .height(request.getHeight())
                .actualWeight(request.getActualWeight())
                .volumetricWeight(breakdown.getVolumetricWeight())
                .billableWeight(breakdown.getBillableWeight())
                .orderType(OrderType.valueOf(request.getOrderType().toUpperCase()))
                .paymentType(PaymentType.valueOf(request.getPaymentType().toUpperCase()))
                .baseCharge(breakdown.getBaseCharge())
                .codSurcharge(breakdown.getCodSurcharge())
                .totalCharge(breakdown.getTotalCharge())
                .status(OrderStatus.CREATED)
                .build();

        order = orderRepository.save(order);

        User actor = userRepository.findById(authenticatedUserId).orElse(null);
        TrackingHistory history = TrackingHistory.builder()
                .order(order)
                .status(OrderStatus.CREATED)
                .changedBy(actor)
                .changedByRole(authenticatedRole)
                .note("Order placed")
                .build();
        trackingHistoryRepository.save(history);

        // Async notification
        notificationService.notifyCustomer(order, OrderStatus.CREATED, null);

        Map<String, Object> response = new HashMap<>();
        response.put("order", order);
        response.put("breakdown", breakdown);
        return response;
    }

    public List<Order> listOrders(OrderStatus status, String zoneId, String agentId, String customerId,
                                  String authenticatedUserId, Role authenticatedRole) {
        if (authenticatedRole == Role.CUSTOMER) {
            return orderRepository.findByCustomerIdOrderByCreatedAtDesc(authenticatedUserId);
        } else if (authenticatedRole == Role.AGENT) {
            return orderRepository.findByAgentIdOrderByCreatedAtDesc(authenticatedUserId);
        } else {
            return orderRepository.filterOrders(status, agentId, customerId, zoneId);
        }
    }

    public Map<String, Object> getOrderDetails(String orderId, String authenticatedUserId, Role authenticatedRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (authenticatedRole == Role.CUSTOMER && !order.getCustomer().getId().equals(authenticatedUserId)) {
            throw new BadRequestException("Access denied");
        }
        if (authenticatedRole == Role.AGENT && (order.getAgent() == null || !order.getAgent().getId().equals(authenticatedUserId))) {
            throw new BadRequestException("Access denied");
        }

        List<TrackingHistory> history = trackingHistoryRepository.findByOrderIdOrderByTimestampAsc(orderId);
        List<RescheduleRequest> reschedules = rescheduleRequestRepository.findByOrderIdOrderByRequestedAtDesc(orderId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.getId());
        result.put("customer", order.getCustomer());
        result.put("agent", order.getAgent());
        result.put("createdBy", order.getCreatedBy());
        result.put("pickupAddress", order.getPickupAddress());
        result.put("pickupPincode", order.getPickupPincode());
        result.put("dropAddress", order.getDropAddress());
        result.put("dropPincode", order.getDropPincode());
        result.put("pickupZone", order.getPickupZone());
        result.put("dropZone", order.getDropZone());
        result.put("length", order.getLength());
        result.put("breadth", order.getBreadth());
        result.put("height", order.getHeight());
        result.put("actualWeight", order.getActualWeight());
        result.put("volumetricWeight", order.getVolumetricWeight());
        result.put("billableWeight", order.getBillableWeight());
        result.put("orderType", order.getOrderType());
        result.put("paymentType", order.getPaymentType());
        result.put("baseCharge", order.getBaseCharge());
        result.put("codSurcharge", order.getCodSurcharge());
        result.put("totalCharge", order.getTotalCharge());
        result.put("status", order.getStatus());
        result.put("scheduledDate", order.getScheduledDate());
        result.put("createdAt", order.getCreatedAt());
        result.put("updatedAt", order.getUpdatedAt());
        result.put("trackingHistory", history);
        result.put("rescheduleRequests", reschedules);

        return result;
    }

    public Map<String, Object> getPublicTracking(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<TrackingHistory> historyList = trackingHistoryRepository.findByOrderIdOrderByTimestampAsc(orderId);
        List<Map<String, Object>> safeHistory = historyList.stream().map(h -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", h.getId());
            entry.put("status", h.getStatus());
            entry.put("changedByRole", h.getChangedByRole());
            entry.put("note", h.getNote());
            entry.put("timestamp", h.getTimestamp());
            return entry;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.getId());
        result.put("status", order.getStatus());
        result.put("orderType", order.getOrderType());
        result.put("paymentType", order.getPaymentType());
        result.put("pickupPincode", order.getPickupPincode());
        result.put("dropPincode", order.getDropPincode());
        result.put("scheduledDate", order.getScheduledDate());
        result.put("createdAt", order.getCreatedAt());
        result.put("updatedAt", order.getUpdatedAt());
        result.put("pickupZone", order.getPickupZone() != null ? Collections.singletonMap("name", order.getPickupZone().getName()) : null);
        result.put("dropZone", order.getDropZone() != null ? Collections.singletonMap("name", order.getDropZone().getName()) : null);
        result.put("trackingHistory", safeHistory);

        return result;
    }

    @Transactional
    public Map<String, Object> autoAssignAgent(String orderId, String actorId, Role actorRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.RESCHEDULED) {
            throw new BadRequestException("Cannot assign agent — order is in " + order.getStatus() + " status");
        }

        String pickupZoneId = order.getPickupZone() != null ? order.getPickupZone().getId() : null;
        Map<String, Object> assignResult = agentAssignmentService.autoAssign(order.getId(), pickupZoneId, actorId, actorRole);

        // Fetch refreshed order to notify customer
        Order refreshedOrder = orderRepository.findById(orderId).orElse(order);
        notificationService.notifyCustomer(refreshedOrder, OrderStatus.AGENT_ASSIGNED, "Agent: " + assignResult.get("agentName"));

        Map<String, Object> response = new HashMap<>(assignResult);
        response.put("message", "Agent auto-assigned");
        return response;
    }

    @Transactional
    public Map<String, Object> manualAssignAgent(String orderId, String agentId, String actorId, Role actorRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Map<String, Object> assignResult = agentAssignmentService.manualAssign(order.getId(), agentId, actorId, actorRole);

        Order refreshedOrder = orderRepository.findById(orderId).orElse(order);
        notificationService.notifyCustomer(refreshedOrder, OrderStatus.AGENT_ASSIGNED, "Agent: " + assignResult.get("agentName"));

        Map<String, Object> response = new HashMap<>(assignResult);
        response.put("message", "Agent manually assigned");
        return response;
    }

    private static final Set<OrderStatus> AGENT_ALLOWED_STATUSES = EnumSet.of(
            OrderStatus.PICKED_UP, OrderStatus.IN_TRANSIT, OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.DELIVERED, OrderStatus.FAILED);

    private static final Set<OrderStatus> ADMIN_ALLOWED_STATUSES = EnumSet.of(
            OrderStatus.PICKED_UP, OrderStatus.IN_TRANSIT, OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.DELIVERED, OrderStatus.FAILED, OrderStatus.CANCELLED);

    @Transactional
    public Map<String, Object> updateOrderStatus(String orderId, StatusUpdateRequest request, String actorId, Role actorRole) {
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid status: " + request.getStatus());
        }

        Set<OrderStatus> allowed = (actorRole == Role.ADMIN) ? ADMIN_ALLOWED_STATUSES : AGENT_ALLOWED_STATUSES;
        if (!allowed.contains(newStatus)) {
            throw new BadRequestException("Cannot set status to " + newStatus);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (actorRole == Role.AGENT && (order.getAgent() == null || !order.getAgent().getId().equals(actorId))) {
            throw new BadRequestException("You are not assigned to this order");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        User actor = userRepository.findById(actorId).orElse(null);
        TrackingHistory history = TrackingHistory.builder()
                .order(order)
                .status(newStatus)
                .changedBy(actor)
                .changedByRole(actorRole)
                .note(request.getNote())
                .build();
        trackingHistoryRepository.save(history);

        // Free up agent if DELIVERED or FAILED
        if ((newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.FAILED) && order.getAgent() != null) {
            agentProfileRepository.findByUserId(order.getAgent().getId()).ifPresent(profile -> {
                profile.setIsAvailable(true);
                agentProfileRepository.save(profile);
            });
        }

        notificationService.notifyCustomer(order, newStatus, request.getNote());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order status updated to " + newStatus);
        return response;
    }

    @Transactional
    public Map<String, Object> rescheduleOrder(String orderId, RescheduleRequestDto request, String actorId, Role actorRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.FAILED) {
            throw new BadRequestException("Only FAILED orders can be rescheduled");
        }

        if (actorRole == Role.CUSTOMER && !order.getCustomer().getId().equals(actorId)) {
            throw new BadRequestException("Access denied");
        }

        LocalDateTime newDate;
        try {
            newDate = LocalDateTime.parse(request.getNewDate(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            try {
                newDate = LocalDateTime.parse(request.getNewDate().substring(0, 19));
            } catch (Exception ex) {
                newDate = LocalDateTime.now().plusDays(1);
            }
        }

        order.setStatus(OrderStatus.RESCHEDULED);
        order.setScheduledDate(newDate);
        order.setAgent(null);
        orderRepository.save(order);

        RescheduleRequest resReq = RescheduleRequest.builder()
                .order(order)
                .newDate(newDate)
                .build();
        rescheduleRequestRepository.save(resReq);

        User actor = userRepository.findById(actorId).orElse(null);
        TrackingHistory history = TrackingHistory.builder()
                .order(order)
                .status(OrderStatus.RESCHEDULED)
                .changedBy(actor)
                .changedByRole(actorRole)
                .note("Rescheduled to " + newDate.toLocalDate().toString())
                .build();
        trackingHistoryRepository.save(history);

        notificationService.notifyCustomer(order, OrderStatus.RESCHEDULED, "New delivery date: " + newDate.toLocalDate().toString());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order rescheduled. Admin will assign a new agent.");
        return response;
    }
}
