package com.example.auth_service.DTO;

import lombok.Data;

@Data
public class ActivationRequest {
    private String username;
    private String code;
}
