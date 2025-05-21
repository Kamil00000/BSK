package com.example.auth_service.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
