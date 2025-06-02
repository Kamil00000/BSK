package com.example.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class VerificationClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${verification.service.url}")
    private String verificationServiceUrl; 


    public void generateActivationCode(String username) {
        String url = verificationServiceUrl + "/verification/generate/activation";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);

        restTemplate.postForObject(url, requestBody, Void.class);
    }
    
}
