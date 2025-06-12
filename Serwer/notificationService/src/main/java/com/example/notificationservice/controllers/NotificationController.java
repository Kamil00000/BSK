package com.example.notificationservice.controller;

import com.example.notificationservice.dto.SendNotificationRequest;
import com.example.notificationservice.dto.UpdatePreferencesRequest;
import com.example.notificationservice.models.Notification;
import com.example.notificationservice.models.NotificationPreference;
import com.example.notificationservice.services.NotificationService;
import com.example.notificationservice.services.NotificationPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationPreferenceService preferenceService;

    // Endpoint called by Chat Service (from diagram: wyslijPowiadomienie)
    @PostMapping("/message")
    public ResponseEntity<?> sendMessageNotification(@RequestBody @Valid SendNotificationRequest request) {
        try {
            notificationService.sendNotification(request);
            return ResponseEntity.ok(Map.of("message", "Notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // General notification endpoint
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody @Valid SendNotificationRequest request) {
        try {
            notificationService.sendNotification(request);
            return ResponseEntity.ok(Map.of("message", "Notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-notifications")
    public ResponseEntity<?> getUserNotifications(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            Page<Notification> notifications = notificationService.getUserNotifications(userId, page, size);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            Long count = notificationService.getUnreadNotificationCount(userId);
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId,
                                        HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/preferences")
    public ResponseEntity<?> getPreferences(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            NotificationPreference preferences = preferenceService.getUserPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> updatePreferences(@RequestBody UpdatePreferencesRequest request,
                                               HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            NotificationPreference preferences = preferenceService.updatePreferences(userId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "Preferences updated successfully",
                    "preferences", preferences
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        // Get userId from JWT token (set by JwtAuthenticationFilter)
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("User ID not found in request");
        }
        return userId;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }
}