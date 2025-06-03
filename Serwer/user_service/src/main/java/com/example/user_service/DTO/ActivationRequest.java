package com.example.user_service.DTO;

import lombok.Data;

@Data
public class ActivationRequest {
    private String username;
    private String code;
}
