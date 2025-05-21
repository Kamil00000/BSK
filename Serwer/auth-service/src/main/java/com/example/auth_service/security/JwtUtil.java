package com.example.auth_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)                 // Ustawia subject (nazwę użytkownika)
                .claim("role", role)  // jedna rola jako String
                .issuedAt(new Date())               // Czas wystawienia
                .expiration(new Date(System.currentTimeMillis() + expiration)) // Czas wygaśnięcia
                .signWith(getSigningKey(), Jwts.SIG.HS256) // Podpis z algorytmem HS256
                .compact();                         // Konwersja na string
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())        // Ustawia klucz weryfikacyjny
                .build()
                .parseSignedClaims(token)           // Parsuje i weryfikuje token
                .getPayload()                       // Pobiera dane (claims)
                .getSubject();                      // Wyciąga subject (username)
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);          // Próba parsowania = walidacja
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;                           // Token nieważny
        }
    }
}