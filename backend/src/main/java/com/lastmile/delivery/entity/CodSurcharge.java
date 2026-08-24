package com.lastmile.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cod_surcharges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodSurcharge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, unique = true)
    private OrderType orderType;

    @Column(name = "surcharge_flat", nullable = false)
    @Builder.Default
    private Double surchargeFlat = 0.0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
