package com.example.chatservice.repositories;

import com.example.chatservice.models.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;


public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.userId = :userId AND p.active = true AND c.active = true")
    List<Chat> findChatsByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Chat c WHERE c.active = true AND " +
            "EXISTS (SELECT 1 FROM ChatParticipant p1 WHERE p1.chat = c AND p1.userId = :user1 AND p1.active = true) AND " +
            "EXISTS (SELECT 1 FROM ChatParticipant p2 WHERE p2.chat = c AND p2.userId = :user2 AND p2.active = true)")
    Optional<Chat> findPrivateChatBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);

    List<Chat> findByCreatedByAndActiveTrue(Long createdBy);
}