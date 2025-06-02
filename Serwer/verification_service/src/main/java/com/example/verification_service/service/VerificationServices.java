package com.example.verification_service.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.verification_service.enums.VerificationType;
import com.example.verification_service.model.VerificationCode;
import com.example.verification_service.repository.VerificationRepository;

@Service
public class VerificationServices {

	@Autowired
    private VerificationRepository repository;

    public String generateCode(String username, VerificationType type) {
        String code = String.format("%06d", new Random().nextInt(999999));
        VerificationCode vc = VerificationCode.builder()
                .userId(username)
                .code(code)
                .type(type)
                .expiration(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();
        repository.save(vc);
        return code;
    }

    public boolean verifyCode(String username, String code) {
        Optional<VerificationCode> result = repository.findValidCode(username, code, LocalDateTime.now());
        result.ifPresent(repository::invalidate);
        return result.isPresent();
    }
}