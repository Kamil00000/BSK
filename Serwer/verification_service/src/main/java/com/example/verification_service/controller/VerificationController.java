package com.example.verification_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.verification_service.DTO.CodeVerificationRequest;
import com.example.verification_service.DTO.VerificationRequest;
import com.example.verification_service.enums.VerificationType;
import com.example.verification_service.service.VerificationCodeEmail;
import com.example.verification_service.service.VerificationServices;

@RestController
@RequestMapping("/verification")
public class VerificationController {
    @Autowired
    private VerificationServices verificationService;
    
    @Autowired
    private VerificationCodeEmail verificationEmailService;

    @PostMapping("/generate/2fa")
    public ResponseEntity<?> generate2FACode(@RequestBody @Valid VerificationRequest request) {
        try {
            String code = verificationService.generateCode(request.getUsername(), VerificationType.LOGIN_2FA);
            verificationEmailService.sendVerificationCode(request.getEmail(), code, "2FA");

            return ResponseEntity.ok("2FA code generated: " + code);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Failed to generate 2FA code: " + e.getMessage());
        }
    }

    @PostMapping("/generate/activation")
    public ResponseEntity<?> generateActivationCode(@RequestBody @Valid VerificationRequest request) {
        try {
            String code = verificationService.generateCode(request.getUsername(), VerificationType.ACTIVATION_CODE);
            verificationEmailService.sendVerificationCode(request.getEmail(), code, "aktywacyjny");

            return ResponseEntity.ok("Activation code generated: " + code);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Failed to generate activation code: " + e.getMessage());
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody @Valid CodeVerificationRequest request) {
        boolean result = verificationService.verifyCode(request.getUsername(), request.getCode());
        if (result) {
            return ResponseEntity.ok("Code verified successfully");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired code");
        }
    }
}

