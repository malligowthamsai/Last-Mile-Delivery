package com.lastmile.delivery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgentAvailabilityRequest {
    @NotNull(message = "isAvailable required")
    private Boolean isAvailable;
}
