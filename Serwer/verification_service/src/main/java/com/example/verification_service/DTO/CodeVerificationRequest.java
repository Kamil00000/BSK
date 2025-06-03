package com.example.verification_service.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodeVerificationRequest {
    @NotBlank(message = "Nazwa użytkownika nie może być pusta")
    private String username;

    @NotBlank(message = "Kod nie może być pusty")
    private String code;
}