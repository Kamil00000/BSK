package com.example.notificationservice.models;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "email_enabled")
    private Boolean emailEnabled = true;

    @Column(name = "push_enabled")
    private Boolean pushEnabled = true;

    @Column(name = "new_message_email")
    private Boolean newMessageEmail = true;

    @Column(name = "new_message_push")
    private Boolean newMessagePush = true;

    @Column(name = "chat_invitation_email")
    private Boolean chatInvitationEmail = true;

    @Column(name = "chat_invitation_push")
    private Boolean chatInvitationPush = true;

    @Column(name = "security_alerts_email")
    private Boolean securityAlertsEmail = true;

    @Column(name = "security_alerts_push")
    private Boolean securityAlertsPush = true;

    @Column(name = "quiet_hours_start")
    private String quietHoursStart = "22:00";

    @Column(name = "quiet_hours_end")
    private String quietHoursEnd = "08:00";

    @Column(name = "respect_quiet_hours")
    private Boolean respectQuietHours = true;

    @Column(name = "language")
    private String language = "pl";
}