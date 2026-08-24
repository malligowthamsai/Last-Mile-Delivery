package com.lastmile.delivery.dto;

import lombok.Data;

@Data
public class AgentUpdateRequest {
    private String zoneId;
    private Boolean isAvailable;
}
