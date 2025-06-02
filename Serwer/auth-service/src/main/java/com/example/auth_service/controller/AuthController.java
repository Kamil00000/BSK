package com.example.auth_service.controller;

import com.example.auth_service.DTO.UserDTO;
import com.example.auth_service.model.JwtResponse;
import com.example.auth_service.model.LoginRequest;
import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.model.TokenRefreshRequest;

import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.service.RefreshTokenService;
import com.example.auth_service.service.TokenBlacklistService;
import com.example.auth_service.service.UserClient;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

	/*
	 * @Autowired private UserRepository userRepository;
	 */
    
    @Autowired
    private UserClient userClient;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    
    @Autowired
    private JwtUtil jwtUtil;

	/*
	 * @PostMapping("/login") public ResponseEntity<?> login(@RequestBody
	 * LoginRequest request) { User user =
	 * userRepository.findByUsername(request.getUsername()) .orElseThrow(() -> new
	 * RuntimeException("Nie znaleziono użytkownika"));
	 * 
	 * if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	 * return
	 * ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nieprawidłowe hasło"); }
	 * 
	 * String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
	 * RefreshToken refreshToken =
	 * refreshTokenService.createRefreshToken(user.getId());
	 * 
	 * return ResponseEntity.ok(new JwtResponse(token, refreshToken.getToken())); }
	 */
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        UserDTO user = userClient.getUserByUsername(request.getUsername());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono użytkownika");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nieprawidłowe hasło");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(new JwtResponse(token, refreshToken.getToken()));
    }

	/*
	 * @PostMapping("/register") public ResponseEntity<?>
	 * register(@RequestBody @Valid User user) { if
	 * (userRepository.findByUsername(user.getUsername()).isPresent()) { return
	 * ResponseEntity.badRequest().body("Username already exists"); }
	 * user.setPassword(passwordEncoder.encode(user.getPassword()));
	 * userRepository.save(user); return ResponseEntity.ok("User registered"); }
	 */
    
	/*
	 * @PostMapping("/refresh") public ResponseEntity<?> refreshToken(@RequestBody
	 * TokenRefreshRequest request) { String requestToken =
	 * request.getRefreshToken();
	 * 
	 * return refreshTokenService.findByToken(requestToken)
	 * .map(refreshTokenService::verifyExpiration) .map(RefreshToken::getUser)
	 * .map(user -> { String token = jwtUtil.generateToken(user.getUsername(),
	 * user.getRole()); return ResponseEntity.ok(new JwtResponse(token,
	 * requestToken)); }) .orElseThrow(() -> new
	 * RuntimeException("Invalid refresh token")); }
	 */
    
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
