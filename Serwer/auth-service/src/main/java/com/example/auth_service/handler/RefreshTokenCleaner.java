package com.example.auth_service.handler;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.auth_service.repository.RefreshTokenRepository;

@Component
public class RefreshTokenCleaner {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleaner(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(fixedRate = 3600000) // co godzinę
    public void removeExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredSince(Instant.now());
    }
}