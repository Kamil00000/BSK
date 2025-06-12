package com.example.notificationservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class UserClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public UserDTO getUserById(Long id) {
        try {
            String url = userServiceUrl + "/users/by-id/" + id;
            return restTemplate.getForObject(url, UserDTO.class);
        } catch (Exception e) {
            log.error("Failed to get user by id {}: {}", id, e.getMessage());
            throw new RuntimeException("User not found: " + id);
        }
    }

    public static class UserDTO {
        private Long id;
        private String username;
        private String email;
        private String role;
        private boolean enabled;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}