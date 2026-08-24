package com.lastmile.delivery.entity;

public enum OrderStatus {
    CREATED,
    AGENT_ASSIGNED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED,
    RESCHEDULED,
    CANCELLED
}
