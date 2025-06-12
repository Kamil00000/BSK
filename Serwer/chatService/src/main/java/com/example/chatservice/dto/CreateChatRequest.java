package com.example.chatservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;


@Data
public class CreateChatRequest {
    @NotBlank(message = "Nazwa czatu jest wymagana")
    private String name;

    private String description;

    private Long receiverId; // For private chats
}
