package com.example.notificationservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendNotificationEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendNewMessageNotification(String toEmail, String senderName, String messageContent) {
        String subject = "Nowa wiadomość od " + senderName;
        String content = String.format(
                "Cześć!\n\n" +
                        "Otrzymałeś nową wiadomość od %s:\n\n" +
                        "\"%s\"\n\n" +
                        "Zaloguj się do aplikacji, aby odpowiedzieć.\n\n" +
                        "Pozdrawienia,\nZespół aplikacji",
                senderName,
                messageContent.length() > 100 ? messageContent.substring(0, 100) + "..." : messageContent
        );

        sendNotificationEmail(toEmail, subject, content);
    }

    public void sendChatInvitationNotification(String toEmail, String inviterName, String chatName) {
        String subject = "Zaproszenie do czatu: " + chatName;
        String content = String.format(
                "Cześć!\n\n" +
                        "%s zaprosił Cię do czatu \"%s\".\n\n" +
                        "Zaloguj się do aplikacji, aby dołączyć do rozmowy.\n\n" +
                        "Pozdrawienia,\nZespół aplikacji",
                inviterName, chatName
        );

        sendNotificationEmail(toEmail, subject, content);
    }
}