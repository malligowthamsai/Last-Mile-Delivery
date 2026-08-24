package com.lastmile.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCalculateResponse {
    
    private ZoneInfo pickupZone;
    private ZoneInfo dropZone;
    private boolean isIntraZone;
    private double estimatedDistanceKm;
    private double volumetricWeight;
    private double billableWeight;
    
    private String selectedRateType;
    private double ratePerKg;
    private double minCharge;
    private double baseCharge;
    private double codSurcharge;
    private double totalCharge;
    private String pricingMethod;
    private String estimatedDelivery;
    
    private List<RateOption> options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneInfo {
        private String zoneId;
        private String zoneName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RateOption {
        private String id;
        private String title;
        private String subtitle;
        private String estimatedDelivery;
        private double ratePerKg;
        private double minCharge;
        private double baseCharge;
        private double codSurcharge;
        private double totalCharge;
        private String pricingMethod;
    }
}
