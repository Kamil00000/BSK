package com.example.chatservice.services;

import com.example.chatservice.models.Message;
import com.example.chatservice.models.ChatParticipant;
import com.example.chatservice.repositories.MessageRepository;
import com.example.chatservice.dto.SendMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;


@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private NotificationClient notificationClient;

    public Message sendMessage(SendMessageRequest request, Long senderId) {
        // Verify user is in chat
        if (!chatService.isUserInChat(request.getChatId(), senderId)) {
            throw new IllegalArgumentException("Użytkownik nie ma dostępu do tego czatu");
        }

        // Encrypt content (szyfrujWiadomosc() from diagram)
        String encryptedContent = encryptionService.encrypt(request.getContent());

        // Create and save message (zapiszWiadomosc() from diagram)
        Message message = Message.builder()
                .chatId(request.getChatId())
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .encryptedContent(encryptedContent)
                .messageType(request.getMessageType())
                .attachmentUrl(request.getAttachmentUrl())
                .build();

        message = messageRepository.save(message);

        // Send notifications (wyslijPowiadomienie() from diagram)
        sendNotifications(message);

        return message;
    }

    private void sendNotifications(Message message) {
        try {
            List<ChatParticipant> participants = chatService.getChatParticipants(message.getChatId());
            for (ChatParticipant participant : participants) {
                if (!participant.getUserId().equals(message.getSenderId())) {
                    notificationClient.sendMessageNotification(participant.getUserId(), message.getContent());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send notifications: " + e.getMessage());
        }
    }

    public Page<Message> getChatMessages(Long chatId, Long userId, int page, int size) {
        if (!chatService.isUserInChat(chatId, userId)) {
            throw new IllegalArgumentException("Użytkownik nie ma dostępu do tego czatu");
        }

        return messageRepository.findByChatIdAndDeletedFalseOrderBySentAtDesc(
                chatId, PageRequest.of(page, size));
    }
}