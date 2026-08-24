package com.lastmile.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AreaRequest {
    @NotBlank(message = "Area name is required")
    private String name;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    @NotBlank(message = "Zone ID is required")
    private String zoneId;
}
