package com.lastmile.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_id")
    private User agent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "pickup_pincode", nullable = false)
    private String pickupPincode;

    @Column(name = "drop_address", nullable = false)
    private String dropAddress;

    @Column(name = "drop_pincode", nullable = false)
    private String dropPincode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pickup_zone_id")
    private Zone pickupZone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "drop_zone_id")
    private Zone dropZone;

    @Column(nullable = false)
    private Double length;

    @Column(nullable = false)
    private Double breadth;

    @Column(nullable = false)
    private Double height;

    @Column(name = "actual_weight", nullable = false)
    private Double actualWeight;

    @Column(name = "volumetric_weight", nullable = false)
    private Double volumetricWeight;

    @Column(name = "billable_weight", nullable = false)
    private Double billableWeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(name = "base_charge", nullable = false)
    private Double baseCharge;

    @Column(name = "cod_surcharge", nullable = false)
    @Builder.Default
    private Double codSurcharge = 0.0;

    @Column(name = "total_charge", nullable = false)
    private Double totalCharge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
