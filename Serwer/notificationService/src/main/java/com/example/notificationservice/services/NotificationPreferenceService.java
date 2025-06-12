package com.example.notificationservice.services;

import com.example.notificationservice.models.NotificationPreference;
import com.example.notificationservice.repositories.NotificationPreferenceRepository;
import com.example.notificationservice.dto.UpdatePreferencesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class NotificationPreferenceService {

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    public NotificationPreference getUserPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    public NotificationPreference updatePreferences(Long userId, UpdatePreferencesRequest request) {
        NotificationPreference preferences = getUserPreferences(userId);

        if (request.getEmailEnabled() != null) preferences.setEmailEnabled(request.getEmailEnabled());
        if (request.getPushEnabled() != null) preferences.setPushEnabled(request.getPushEnabled());
        if (request.getNewMessageEmail() != null) preferences.setNewMessageEmail(request.getNewMessageEmail());
        if (request.getNewMessagePush() != null) preferences.setNewMessagePush(request.getNewMessagePush());
        if (request.getChatInvitationEmail() != null) preferences.setChatInvitationEmail(request.getChatInvitationEmail());
        if (request.getChatInvitationPush() != null) preferences.setChatInvitationPush(request.getChatInvitationPush());
        if (request.getSecurityAlertsEmail() != null) preferences.setSecurityAlertsEmail(request.getSecurityAlertsEmail());
        if (request.getSecurityAlertsPush() != null) preferences.setSecurityAlertsPush(request.getSecurityAlertsPush());
        if (request.getQuietHoursStart() != null) preferences.setQuietHoursStart(request.getQuietHoursStart());
        if (request.getQuietHoursEnd() != null) preferences.setQuietHoursEnd(request.getQuietHoursEnd());
        if (request.getRespectQuietHours() != null) preferences.setRespectQuietHours(request.getRespectQuietHours());
        if (request.getLanguage() != null) preferences.setLanguage(request.getLanguage());

        return preferenceRepository.save(preferences);
    }

    private NotificationPreference createDefaultPreferences(Long userId) {
        NotificationPreference preferences = NotificationPreference.builder()
                .userId(userId)
                .emailEnabled(true)
                .pushEnabled(true)
                .newMessageEmail(true)
                .newMessagePush(true)
                .chatInvitationEmail(true)
                .chatInvitationPush(true)
                .securityAlertsEmail(true)
                .securityAlertsPush(true)
                .quietHoursStart("22:00")
                .quietHoursEnd("08:00")
                .respectQuietHours(true)
                .language("pl")
                .build();

        return preferenceRepository.save(preferences);
    }

    public boolean shouldSendEmailNotification(Long userId, String notificationType) {
        NotificationPreference preferences = getUserPreferences(userId);

        if (!preferences.getEmailEnabled()) return false;

        switch (notificationType) {
            case "NEW_MESSAGE":
                return preferences.getNewMessageEmail();
            case "CHAT_INVITATION":
                return preferences.getChatInvitationEmail();
            case "SECURITY_ALERT":
                return preferences.getSecurityAlertsEmail();
            default:
                return false;
        }
    }

    public boolean shouldSendPushNotification(Long userId, String notificationType) {
        NotificationPreference preferences = getUserPreferences(userId);

        if (!preferences.getPushEnabled()) return false;

        switch (notificationType) {
            case "NEW_MESSAGE":
                return preferences.getNewMessagePush();
            case "CHAT_INVITATION":
                return preferences.getChatInvitationPush();
            case "SECURITY_ALERT":
                return preferences.getSecurityAlertsPush();
            default:
                return false;
        }
    }
}