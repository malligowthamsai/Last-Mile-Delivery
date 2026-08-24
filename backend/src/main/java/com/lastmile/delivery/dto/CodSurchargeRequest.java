package com.lastmile.delivery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CodSurchargeRequest {
    @NotNull(message = "surchargeFlat must be >= 0")
    @DecimalMin(value = "0.0", message = "surchargeFlat must be >= 0")
    private Double surchargeFlat;
}
