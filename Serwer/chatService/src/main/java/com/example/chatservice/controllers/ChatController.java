package com.example.chatservice.controller;

import com.example.chatservice.dto.CreateChatRequest;
import com.example.chatservice.dto.SendMessageRequest;
import com.example.chatservice.dto.AddParticipantRequest;
import com.example.chatservice.dto.UserDTO;
import com.example.chatservice.models.Chat;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.ChatService;
import com.example.chatservice.services.MessageService;
import com.example.chatservice.services.UserClient;
import com.example.chatservice.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<?> createChat(@RequestBody @Valid CreateChatRequest request,
                                        HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            Chat chat = chatService.createChat(request, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Czat utworzony pomyślnie",
                    "chatId", chat.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/addUser")
    public ResponseEntity<?> addParticipant(@RequestBody AddParticipantRequest request,
                                            @RequestParam Long chatId,
                                            HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);

            // Verify user has permission to add participants
            if (!chatService.isUserInChat(chatId, userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Brak uprawnień"));
            }

            chatService.addParticipant(chatId, request.getUserId());
            return ResponseEntity.ok(Map.of("message", "Użytkownik dodany do czatu"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveChat(@RequestParam Long chatId,
                                       HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            chatService.removeParticipant(chatId, userId);
            return ResponseEntity.ok(Map.of("message", "Opuściłeś czat"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody @Valid SendMessageRequest request,
                                         HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            Message message = messageService.sendMessage(request, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Wiadomość wysłana",
                    "messageId", message.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getChatMessages(@RequestParam Long chatId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "50") int size,
                                             HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            Page<Message> messages = messageService.getChatMessages(chatId, userId, page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-chats")
    public ResponseEntity<?> getUserChats(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            List<Chat> chats = chatService.getUserChats(userId);
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Brak tokena autoryzacyjnego");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        UserDTO user = userClient.getUserByUsername(username);

        if (user == null) {
            throw new IllegalStateException("Użytkownik nie znaleziony: " + username);
        }
        return user.getId();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }
}