package com.example.auth_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.auth_service.DTO.UserDTO;



@Service
public class UserClient {

	@Autowired
    private RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl; 

    public UserDTO getUserByUsername(String username) {
        String url = userServiceUrl + "/users/by-username/" + username;
        return restTemplate.getForObject(url, UserDTO.class);
    }
    
    public UserDTO getUserById(Long id) {
        String url = userServiceUrl + "/users/by-id/" + id;
        return restTemplate.getForObject(url, UserDTO.class);
    }
    
}
