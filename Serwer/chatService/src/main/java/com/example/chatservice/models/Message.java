package com.example.chatservice.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "encrypted_content", columnDefinition = "TEXT")
    private String encryptedContent;

    @Column(name = "message_type")
    private String messageType = "TEXT";

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (deleted == null) {
            deleted = false;
        }
    }
}