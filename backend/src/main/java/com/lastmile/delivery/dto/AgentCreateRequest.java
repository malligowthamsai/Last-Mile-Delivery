package com.lastmile.delivery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AgentCreateRequest {
    @NotBlank(message = "Name required")
    private String name;

    @NotBlank(message = "Valid email required")
    @Email(message = "Valid email required")
    private String email;

    @NotBlank(message = "Password min 6 chars")
    @Size(min = 6, message = "Password min 6 chars")
    private String password;

    private String phone;

    @NotBlank(message = "zoneId required")
    private String zoneId;
}
