package com.example.auth_service.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.service.RefreshTokenService;
import com.example.auth_service.service.TokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtLogoutHandler implements LogoutHandler {
	
    private JwtUtil jwtUtil;

    private RefreshTokenService refreshTokenService;
    
    private final TokenBlacklistService tokenBlacklistService;

    public JwtLogoutHandler(TokenBlacklistService tokenBlacklistService, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, 
                      Authentication authentication) {
        String token = extractToken(request);
        if (token != null) {
            tokenBlacklistService.blacklistToken(token);
            String username = jwtUtil.extractUsername(token);
            refreshTokenService.deleteByUsername(username);
        }
        
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
