package com.example.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationClient verificationClient;
    
    
    public void registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Użytkownik o zadanej nazwie już isntieje");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        
        verificationClient.generateActivationCode(user.getUsername(), user.getEmail());

    }
    
    public void activateUser(String username, String code) {
        boolean valid = verificationClient.verifyActivationCode(username, code);
        if (!valid) {
            throw new IllegalArgumentException("Niepoprawny lub wygasły kod aktywacyjny");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nieznaleziony"));

        user.setEnabled(true);
        userRepository.save(user);
    }

    
}
