package com.lastmile.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ZoneRequest {
    @NotBlank(message = "Zone name is required")
    private String name;
}
