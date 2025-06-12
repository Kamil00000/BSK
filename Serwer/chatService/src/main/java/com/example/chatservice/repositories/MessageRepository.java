package com.example.chatservice.repositories;

import com.example.chatservice.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatIdAndDeletedFalseOrderBySentAtDesc(Long chatId, Pageable pageable);
}