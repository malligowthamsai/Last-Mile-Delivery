package com.lastmile.delivery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateRequest {
    @NotBlank(message = "Pickup address required")
    private String pickupAddress;

    @NotBlank(message = "Pickup pincode required")
    private String pickupPincode;

    @NotBlank(message = "Drop address required")
    private String dropAddress;

    @NotBlank(message = "Drop pincode required")
    private String dropPincode;

    @NotNull(message = "Length must be positive")
    @DecimalMin(value = "0.1", message = "Length must be positive")
    private Double length;

    @NotNull(message = "Breadth must be positive")
    @DecimalMin(value = "0.1", message = "Breadth must be positive")
    private Double breadth;

    @NotNull(message = "Height must be positive")
    @DecimalMin(value = "0.1", message = "Height must be positive")
    private Double height;

    @NotNull(message = "Actual weight must be positive")
    @DecimalMin(value = "0.01", message = "Actual weight must be positive")
    private Double actualWeight;

    @NotBlank(message = "orderType required")
    private String orderType;

    @NotBlank(message = "paymentType required")
    private String paymentType;

    private String rateType = "STANDARD";

    private String customerId; // Optional, set only by Admin creating on behalf of customer
}
