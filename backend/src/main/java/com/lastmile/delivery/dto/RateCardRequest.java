package com.lastmile.delivery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RateCardRequest {
    @NotBlank(message = "zoneFromId required")
    private String zoneFromId;

    @NotBlank(message = "zoneToId required")
    private String zoneToId;

    @NotBlank(message = "orderType must be B2B or B2C")
    private String orderType;

    @NotNull(message = "ratePerKg must be a positive number")
    @DecimalMin(value = "0.0", message = "ratePerKg must be >= 0")
    private Double ratePerKg;

    private Double minCharge = 0.0;
}
