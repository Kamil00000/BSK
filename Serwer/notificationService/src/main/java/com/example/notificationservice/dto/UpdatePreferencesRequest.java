package com.example.notificationservice.dto;

import lombok.Data;


@Data
public class UpdatePreferencesRequest {
    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean newMessageEmail;
    private Boolean newMessagePush;
    private Boolean chatInvitationEmail;
    private Boolean chatInvitationPush;
    private Boolean securityAlertsEmail;
    private Boolean securityAlertsPush;
    private String quietHoursStart;
    private String quietHoursEnd;
    private Boolean respectQuietHours;
    private String language;
}