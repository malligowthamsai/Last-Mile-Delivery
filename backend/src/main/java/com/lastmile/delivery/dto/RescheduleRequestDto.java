package com.lastmile.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RescheduleRequestDto {
    @NotBlank(message = "Valid ISO date required")
    private String newDate;
}
