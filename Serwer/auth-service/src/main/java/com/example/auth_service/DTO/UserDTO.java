package com.example.auth_service.DTO;

import lombok.Data;

@Data
public class UserDTO {
 private Long id;
 private String username;
 private String password;
 private String role;
 private boolean enabled;
 private boolean twoFactorEnabled;
}