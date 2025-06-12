package com.example.chatservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;


@Service
public class NotificationClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    public void sendMessageNotification(Long userId, String content) {
        try {
            String url = notificationServiceUrl + "/notifications/message";

            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("message", content);
            request.put("type", "NEW_MESSAGE");

            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}