package com.example.notificationservice;

import com.example.notificationservice.dto.SendNotificationRequest;
import com.example.notificationservice.dto.UpdatePreferencesRequest;
import com.example.notificationservice.models.Notification;
import com.example.notificationservice.models.NotificationPreference;
import com.example.notificationservice.repositories.NotificationRepository;
import com.example.notificationservice.repositories.NotificationPreferenceRepository;
import com.example.notificationservice.services.EmailService;
import com.example.notificationservice.services.NotificationService;
import com.example.notificationservice.services.NotificationPreferenceService;
import com.example.notificationservice.services.UserClient;
import com.example.notificationservice.services.UserClient.UserDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "eureka.client.enabled=false",
        "user.service.url=http://localhost:8082",
        "spring.mail.host=localhost",
        "spring.mail.port=25",
        "spring.mail.username=test@test.com"
})
@Transactional
public class NotificationServiceApplicationTests {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationPreferenceService preferenceService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @MockBean
    private UserClient userClient;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Przygotuj testowych użytkowników
        UserDTO user1 = new UserDTO();
        user1.setId(1L);
        user1.setUsername("testuser1");
        user1.setEmail("test1@test.com");
        user1.setRole("USER");
        user1.setEnabled(true);

        UserDTO user2 = new UserDTO();
        user2.setId(2L);
        user2.setUsername("testuser2");
        user2.setEmail("test2@test.com");
        user2.setRole("USER");
        user2.setEnabled(true);

        // Mock odpowiedzi UserClient
        when(userClient.getUserById(1L)).thenReturn(user1);
        when(userClient.getUserById(2L)).thenReturn(user2);

        // Mock EmailService
        doNothing().when(emailService).sendNewMessageNotification(anyString(), anyString(), anyString());
        doNothing().when(emailService).sendChatInvitationNotification(anyString(), anyString(), anyString());
        doNothing().when(emailService).sendNotificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void contextLoads() {
        // Test podstawowy - sprawdza czy kontekst Spring się ładuje
        assertNotNull(notificationService);
        assertNotNull(preferenceService);
    }

    @Test
    void shouldSendNewMessageNotification() {
        // Given
        SendNotificationRequest request = new SendNotificationRequest();
        request.setUserId(1L);
        request.setMessage("Test notification message");
        request.setType("NEW_MESSAGE");
        request.setTitle("Test Title");

        // When
        notificationService.sendNotification(request);

        // Then
        assertEquals(1, notificationRepository.count());
        Notification notification = notificationRepository.findAll().get(0);
        assertEquals("Test notification message", notification.getContent());
        assertEquals("NEW_MESSAGE", notification.getNotificationType());
        assertEquals(1L, notification.getUserId());
        assertEquals("Test Title", notification.getTitle());

        // Sprawdź czy email został wysłany
        verify(emailService, times(1)).sendNewMessageNotification(
                eq("test1@test.com"), anyString(), eq("Test notification message")
        );
    }

    @Test
    void shouldSendChatInvitationNotification() {
        // Given
        SendNotificationRequest request = new SendNotificationRequest();
        request.setUserId(2L);
        request.setMessage("You have been invited to join a chat");
        request.setType("CHAT_INVITATION");
        request.setMetadata("{\"inviterName\":\"testuser1\",\"chatName\":\"Test Chat\"}");

        // When
        notificationService.sendNotification(request);

        // Then
        assertEquals(1, notificationRepository.count());
        Notification notification = notificationRepository.findAll().get(0);
        assertEquals("CHAT_INVITATION", notification.getNotificationType());
        assertEquals(2L, notification.getUserId());
        assertTrue(notification.getSentViaEmail());

        // Sprawdź czy email został wysłany
        verify(emailService, times(1)).sendChatInvitationNotification(
                eq("test2@test.com"), anyString(), anyString()
        );
    }

    @Test
    void shouldCreateDefaultPreferences() {
        // When
        NotificationPreference preferences = preferenceService.getUserPreferences(1L);

        // Then
        assertNotNull(preferences);
        assertEquals(1L, preferences.getUserId());
        assertTrue(preferences.getEmailEnabled());
        assertTrue(preferences.getPushEnabled());
        assertTrue(preferences.getNewMessageEmail());
        assertEquals("pl", preferences.getLanguage());
        assertEquals("22:00", preferences.getQuietHoursStart());
        assertEquals("08:00", preferences.getQuietHoursEnd());
    }

    @Test
    void shouldUpdateUserPreferences() {
        // Given
        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        request.setEmailEnabled(false);
        request.setNewMessagePush(false);
        request.setLanguage("en");
        request.setQuietHoursStart("23:00");
        request.setQuietHoursEnd("07:00");

        // When
        NotificationPreference updated = preferenceService.updatePreferences(1L, request);

        // Then
        assertNotNull(updated);
        assertFalse(updated.getEmailEnabled());
        assertFalse(updated.getNewMessagePush());
        assertEquals("en", updated.getLanguage());
        assertEquals("23:00", updated.getQuietHoursStart());
        assertEquals("07:00", updated.getQuietHoursEnd());

        // Sprawdź czy zostało zapisane w bazie
        NotificationPreference fromDb = preferenceRepository.findByUserId(1L).orElse(null);
        assertNotNull(fromDb);
        assertFalse(fromDb.getEmailEnabled());
        assertEquals("en", fromDb.getLanguage());
    }

    @Test
    void shouldNotSendEmailWhenDisabledInPreferences() {
        // Given - wyłącz email w preferencjach
        NotificationPreference preferences = NotificationPreference.builder()
                .userId(1L)
                .emailEnabled(false)
                .pushEnabled(true)
                .newMessageEmail(false)
                .newMessagePush(true)
                .build();
        preferenceRepository.save(preferences);

        SendNotificationRequest request = new SendNotificationRequest();
        request.setUserId(1L);
        request.setMessage("Test message");
        request.setType("NEW_MESSAGE");

        // When
        notificationService.sendNotification(request);

        // Then
        verify(emailService, never()).sendNewMessageNotification(anyString(), anyString(), anyString());

        // Ale powiadomienie powinno być zapisane
        assertEquals(1, notificationRepository.count());
        Notification notification = notificationRepository.findAll().get(0);
        assertFalse(notification.getSentViaEmail());
    }

    @Test
    void shouldGetUnreadNotificationCount() {
        // Given - utwórz testowe powiadomienia
        createTestNotification(1L, "Message 1", "NEW_MESSAGE");
        createTestNotification(1L, "Message 2", "NEW_MESSAGE");
        createTestNotification(2L, "Message 3", "NEW_MESSAGE"); // dla innego użytkownika

        // When
        Long count = notificationService.getUnreadNotificationCount(1L);

        // Then
        assertEquals(2L, count);
    }

    @Test
    void shouldMarkNotificationAsRead() {
        // Given
        Notification notification = createTestNotification(1L, "Test message", "NEW_MESSAGE");
        assertFalse(notification.getReadStatus());

        // When
        notificationService.markAsRead(notification.getId(), 1L);

        // Then
        Notification updated = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(updated);
        assertTrue(updated.getReadStatus());
        assertNotNull(updated.getReadAt());
    }

    @Test
    void shouldNotMarkOtherUserNotificationAsRead() {
        // Given - powiadomienie dla user2
        Notification notification = createTestNotification(2L, "Test message", "NEW_MESSAGE");

        // When/Then - próba oznaczenia przez user1 powinna rzucić wyjątek
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(notification.getId(), 1L);
        });
    }

    @Test
    void shouldHandleUserNotFound() {
        // Given - mock UserClient to throw exception
        when(userClient.getUserById(999L)).thenThrow(new RuntimeException("User not found: 999"));

        SendNotificationRequest request = new SendNotificationRequest();
        request.setUserId(999L);
        request.setMessage("Test message");
        request.setType("NEW_MESSAGE");

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            notificationService.sendNotification(request);
        });
    }

    @Test
    void shouldCheckNotificationPreferences() {
        // Given
        Long userId = 1L;

        // When - domyślne preferencje
        boolean shouldSendEmail = preferenceService.shouldSendEmailNotification(userId, "NEW_MESSAGE");
        boolean shouldSendPush = preferenceService.shouldSendPushNotification(userId, "NEW_MESSAGE");

        // Then
        assertTrue(shouldSendEmail);
        assertTrue(shouldSendPush);

        // When - wyłącz email dla nowych wiadomości
        UpdatePreferencesRequest request = new UpdatePreferencesRequest();
        request.setNewMessageEmail(false);
        preferenceService.updatePreferences(userId, request);

        boolean shouldSendEmailAfter = preferenceService.shouldSendEmailNotification(userId, "NEW_MESSAGE");
        boolean shouldSendPushAfter = preferenceService.shouldSendPushNotification(userId, "NEW_MESSAGE");

        // Then
        assertFalse(shouldSendEmailAfter);
        assertTrue(shouldSendPushAfter); // push nadal włączony
    }

    // Helper method
    private Notification createTestNotification(Long userId, String content, String type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title("Test Title")
                .content(content)
                .notificationType(type)
                .sentViaEmail(false)
                .sentViaPush(false)
                .readStatus(false)
                .build();
        return notificationRepository.save(notification);
    }
}