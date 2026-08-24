package com.lastmile.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "rate_cards",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"zone_from_id", "zone_to_id", "order_type"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_from_id", nullable = false)
    private Zone zoneFrom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_to_id", nullable = false)
    private Zone zoneTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Column(name = "rate_per_kg", nullable = false)
    private Double ratePerKg;

    @Column(name = "min_charge", nullable = false)
    @Builder.Default
    private Double minCharge = 0.0;

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
