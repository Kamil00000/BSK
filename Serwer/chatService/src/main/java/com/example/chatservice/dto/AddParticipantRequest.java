package com.example.chatservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;


@Data
public class AddParticipantRequest {
    @NotNull(message = "ID użytkownika jest wymagane")
    private Long userId;
}
