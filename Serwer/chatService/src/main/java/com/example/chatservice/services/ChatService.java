package com.example.chatservice.services;

import com.example.chatservice.models.Chat;
import com.example.chatservice.models.ChatParticipant;
import com.example.chatservice.repositories.ChatRepository;
import com.example.chatservice.repositories.ChatParticipantRepository;
import com.example.chatservice.dto.CreateChatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatParticipantRepository participantRepository;

    public Chat createChat(CreateChatRequest request, Long creatorId) {
        // Check if private chat already exists
        if (request.getReceiverId() != null) {
            Optional<Chat> existingChat = chatRepository.findPrivateChatBetweenUsers(creatorId, request.getReceiverId());
            if (existingChat.isPresent()) {
                return existingChat.get();
            }
        }

        Chat chat = Chat.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(creatorId)
                .active(true)
                .build();

        chat = chatRepository.save(chat);

        // Add creator as participant
        addParticipant(chat.getId(), creatorId);

        // Add receiver if private chat
        if (request.getReceiverId() != null) {
            addParticipant(chat.getId(), request.getReceiverId());
        }

        return chat;
    }

    public void addParticipant(Long chatId, Long userId) {
        if (participantRepository.existsByChatIdAndUserIdAndActiveTrue(chatId, userId)) {
            throw new IllegalArgumentException("Użytkownik już jest uczestnikiem tego czatu");
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Czat nie znaleziony"));

        ChatParticipant participant = ChatParticipant.builder()
                .chat(chat)
                .userId(userId)
                .active(true)
                .build();

        participantRepository.save(participant);
    }

    public void removeParticipant(Long chatId, Long userId) {
        ChatParticipant participant = participantRepository.findByChatIdAndUserIdAndActiveTrue(chatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Uczestnik nie znaleziony"));

        participant.setActive(false);
        participant.setLeftAt(LocalDateTime.now());
        participantRepository.save(participant);
    }

    public List<Chat> getUserChats(Long userId) {
        return chatRepository.findChatsByUserId(userId);
    }

    public List<ChatParticipant> getChatParticipants(Long chatId) {
        return participantRepository.findByChatIdAndActiveTrue(chatId);
    }

    public boolean isUserInChat(Long chatId, Long userId) {
        return participantRepository.existsByChatIdAndUserIdAndActiveTrue(chatId, userId);
    }

    public Chat getChatById(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Czat nie znaleziony"));
    }
}