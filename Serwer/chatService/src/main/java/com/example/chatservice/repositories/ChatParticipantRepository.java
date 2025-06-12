package com.example.chatservice.repositories;

import com.example.chatservice.models.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByChatIdAndActiveTrue(Long chatId);

    Optional<ChatParticipant> findByChatIdAndUserIdAndActiveTrue(Long chatId, Long userId);

    boolean existsByChatIdAndUserIdAndActiveTrue(Long chatId, Long userId);
}