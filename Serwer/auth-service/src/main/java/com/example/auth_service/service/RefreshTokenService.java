package com.example.auth_service.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.DTO.UserDTO;
import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.repository.RefreshTokenRepository;

@Service
@Transactional
public class RefreshTokenService {

    @Value("${jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserClient userClient;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserClient userClient) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userClient = userClient;
    }
    
    public RefreshToken createRefreshToken(Long userId) {
        UserDTO user = userClient.getUserById(userId);
        if (user == null) throw new RuntimeException("User not found");

        RefreshToken token = new RefreshToken();
        token.setUserId(user.getId());
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        token.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
    
    public void deleteByUsername(String username) {
        UserDTO user = userClient.getUserByUsername(username);
        if (user != null) {
            refreshTokenRepository.deleteByUserId(user.getId());
        }
    }
}
