package com.example.notificationservice.services;

import com.example.notificationservice.models.Notification;
import com.example.notificationservice.repositories.NotificationRepository;
import com.example.notificationservice.dto.SendNotificationRequest;
import com.example.notificationservice.services.UserClient.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationPreferenceService preferenceService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserClient userClient;

    public void sendNotification(SendNotificationRequest request) {
        log.info("Processing notification request for user: {}, type: {}", request.getUserId(), request.getType());

        try {
            // Get user info
            UserDTO user = userClient.getUserById(request.getUserId());

            // Check preferences (zgodnie z diagramem: SELECT ustawienia_powiadomien WHERE user=?)
            boolean shouldSendEmail = preferenceService.shouldSendEmailNotification(request.getUserId(), request.getType());
            boolean shouldSendPush = preferenceService.shouldSendPushNotification(request.getUserId(), request.getType());

            // Create notification record
            Notification notification = Notification.builder()
                    .userId(request.getUserId())
                    .title(request.getTitle() != null ? request.getTitle() : getDefaultTitle(request.getType()))
                    .content(request.getMessage())
                    .notificationType(request.getType())
                    .metadata(request.getMetadata())
                    .build();

            // Send email if enabled
            if (shouldSendEmail && user.getEmail() != null) {
                try {
                    sendEmailNotification(user, request);
                    notification.setSentViaEmail(true);
                    log.info("Email notification sent to user: {}", request.getUserId());
                } catch (Exception e) {
                    log.error("Failed to send email notification to user {}: {}", request.getUserId(), e.getMessage());
                }
            }

            // Send push if enabled (placeholder for now)
            if (shouldSendPush) {
                try {
                    sendPushNotification(user, request);
                    notification.setSentViaPush(true);
                    log.info("Push notification sent to user: {}", request.getUserId());
                } catch (Exception e) {
                    log.error("Failed to send push notification to user {}: {}", request.getUserId(), e.getMessage());
                }
            }

            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Notification processed successfully for user: {}", request.getUserId());

        } catch (Exception e) {
            log.error("Failed to process notification for user {}: {}", request.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    private void sendEmailNotification(UserDTO user, SendNotificationRequest request) {
        switch (request.getType()) {
            case "NEW_MESSAGE":
                // Parse metadata to get sender info
                String senderName = "Nieznany użytkownik"; // Default
                try {
                    // In real implementation, parse JSON metadata
                    senderName = extractSenderNameFromMetadata(request.getMetadata());
                } catch (Exception e) {
                    log.warn("Failed to parse sender name from metadata");
                }
                emailService.sendNewMessageNotification(user.getEmail(), senderName, request.getMessage());
                break;

            case "CHAT_INVITATION":
                String inviterName = "Nieznany użytkownik";
                String chatName = "Czat";
                try {
                    // In real implementation, parse JSON metadata
                    inviterName = extractInviterNameFromMetadata(request.getMetadata());
                    chatName = extractChatNameFromMetadata(request.getMetadata());
                } catch (Exception e) {
                    log.warn("Failed to parse invitation details from metadata");
                }
                emailService.sendChatInvitationNotification(user.getEmail(), inviterName, chatName);
                break;

            default:
                emailService.sendNotificationEmail(
                        user.getEmail(),
                        request.getTitle() != null ? request.getTitle() : "Powiadomienie",
                        request.getMessage()
                );
        }
    }

    private void sendPushNotification(UserDTO user, SendNotificationRequest request) {
        // Placeholder for push notification implementation
        log.info("Push notification would be sent to user: {} (not implemented)", user.getUsername());
    }

    private String getDefaultTitle(String type) {
        switch (type) {
            case "NEW_MESSAGE": return "Nowa wiadomość";
            case "CHAT_INVITATION": return "Zaproszenie do czatu";
            case "SECURITY_ALERT": return "Alert bezpieczeństwa";
            default: return "Powiadomienie";
        }
    }

    private String extractSenderNameFromMetadata(String metadata) {
        // Simple implementation - in real app use JSON parser
        if (metadata != null && metadata.contains("senderName")) {
            // Parse JSON properly in real implementation
            return "Użytkownik"; // Placeholder
        }
        return "Nieznany użytkownik";
    }

    private String extractInviterNameFromMetadata(String metadata) {
        // Simple implementation - in real app use JSON parser
        return "Użytkownik"; // Placeholder
    }

    private String extractChatNameFromMetadata(String metadata) {
        // Simple implementation - in real app use JSON parser
        return "Czat"; // Placeholder
    }

    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public Long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndReadStatusFalse(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Notification does not belong to user");
        }

        notification.setReadStatus(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}