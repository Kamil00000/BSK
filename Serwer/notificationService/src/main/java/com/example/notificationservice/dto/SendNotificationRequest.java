package com.example.notificationservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;


@Data
public class SendNotificationRequest {
    @NotNull(message = "ID użytkownika jest wymagane")
    private Long userId;

    @NotBlank(message = "Wiadomość jest wymagana")
    private String message;

    @NotBlank(message = "Typ powiadomienia jest wymagany")
    private String type;

    private String title;

    private String metadata; // JSON string for additional data
}