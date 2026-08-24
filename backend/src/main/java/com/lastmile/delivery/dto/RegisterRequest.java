package com.lastmile.delivery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Valid email required")
    @Email(message = "Valid email required")
    private String email;

    @NotBlank(message = "Password must be at least 6 chars")
    @Size(min = 6, message = "Password must be at least 6 chars")
    private String password;

    private String phone;
}
