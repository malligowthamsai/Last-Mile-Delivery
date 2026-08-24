package com.lastmile.delivery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCalculateRequest {
    @NotBlank(message = "Pickup pincode is required")
    private String pickupPincode;

    @NotBlank(message = "Drop pincode is required")
    private String dropPincode;

    @NotNull(message = "Length is required")
    @DecimalMin(value = "0.1", message = "Length must be positive")
    private Double length;

    @NotNull(message = "Breadth is required")
    @DecimalMin(value = "0.1", message = "Breadth must be positive")
    private Double breadth;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "0.1", message = "Height must be positive")
    private Double height;

    @NotNull(message = "Actual weight is required")
    @DecimalMin(value = "0.01", message = "Actual weight must be positive")
    private Double actualWeight;

    @NotBlank(message = "orderType is required (B2B or B2C)")
    private String orderType;

    @NotBlank(message = "paymentType is required (PREPAID or COD)")
    private String paymentType;

    private String rateType = "STANDARD";
}
