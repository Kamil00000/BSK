package com.example.chatservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;


@Data
public class SendMessageRequest {
    @NotNull(message = "ID czatu jest wymagane")
    private Long chatId;

    private Long receiverId;

    @NotBlank(message = "Treść wiadomości jest wymagana")
    @Size(max = 4000, message = "Wiadomość może mieć maksymalnie 4000 znaków")
    private String content;

    private String messageType = "TEXT";

    private String attachmentUrl;
}