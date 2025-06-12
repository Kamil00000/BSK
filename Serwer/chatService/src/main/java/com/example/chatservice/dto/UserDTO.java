package com.example.chatservice.dto;

import lombok.Data;

@Data
public class UserDTO {
 private Long id;
 private String username;
 private String email;
 private String password;
 private String role;
 private boolean enabled;
 private boolean twoFactorEnabled;
}