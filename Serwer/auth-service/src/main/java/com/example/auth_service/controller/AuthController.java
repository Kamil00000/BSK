package com.example.auth_service.controller;

import com.example.auth_service.DTO.UserDTO;
import com.example.auth_service.model.JwtResponse;
import com.example.auth_service.model.LoginRequest;
import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.model.TokenRefreshRequest;

import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.service.AuthServices;
import com.example.auth_service.service.RefreshTokenService;
import com.example.auth_service.service.TokenBlacklistService;
import com.example.auth_service.service.UserClient;
import com.example.auth_service.DTO.ActivationRequest;

import io.jsonwebtoken.JwtException;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserClient userClient;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Autowired
    private AuthServices authService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        UserDTO user = userClient.getUserByUsername(request.getUsername());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono użytkownika");
        }

        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Nieaktywowane konto");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nieprawidłowe hasło");
        }

        if (user.isTwoFactorEnabled()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("To konto wymaga logowania przez 2FA");
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(new JwtResponse(token, refreshToken.getToken()));
    }

    
    @PostMapping("/login/2fa")
    public ResponseEntity<?> loginWith2FA(@RequestBody LoginRequest request) {
        UserDTO user = userClient.getUserByUsername(request.getUsername());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono użytkownika");
        }

        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Nieaktywowane konto");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nieprawidłowe hasło");
        }

        if (!user.isTwoFactorEnabled()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("To konto nie wymaga 2FA. Użyj endpointu /login");
        }

        authService.generateUser2fa(user.getUsername(),user.getEmail());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body( "Kod 2FA wygenerowany zweryfikuj /login/2fa/verify");
    }

    @PostMapping("/login/2fa/verify")
    public ResponseEntity<?> verify2FA(@RequestBody ActivationRequest request) {
        try {
            boolean verified = authService.verifyUser2fa(request.getUsername(), request.getCode());

            if (!verified) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Niepoprawny kod 2FA");
            }

            UserDTO user = userClient.getUserByUsername(request.getUsername());

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            return ResponseEntity.ok(new JwtResponse(token, refreshToken.getToken()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        String requestToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestToken)
            .map(refreshTokenService::verifyExpiration)
            .map(rt -> {
                UserDTO user = userClient.getUserById(rt.getUserId());
                String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                return ResponseEntity.ok(new JwtResponse(token, requestToken));
            })
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        try {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is blacklisted");
            }
            jwtUtil.validateToken(token);
            return ResponseEntity.ok("Token is valid");
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }
}
