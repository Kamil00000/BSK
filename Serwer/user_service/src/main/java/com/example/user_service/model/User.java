package com.example.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    @Size(min = 3, max = 50, message = "Nazwa użytkownika musi być pomiędzy 3 a 50 znaków")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Email jest wymagany")
    @Column(unique = true)
    private String email;
    
    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 6, message = "Hasło musi być przynajmniej 6 znakowe")
    private String password;

    @NotBlank(message = "Rola jest wymagana")
    private String role;

    private boolean enabled = false;

    private boolean twoFactorEnabled = false;
}