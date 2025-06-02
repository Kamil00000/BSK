package com.example.verification_service.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificationRequest {
    @NotBlank(message = "Username cannot be blank")
    private String username;
}