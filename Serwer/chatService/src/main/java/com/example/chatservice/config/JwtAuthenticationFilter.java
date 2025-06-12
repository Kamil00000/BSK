package com.example.chatservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // Endpoints that should skip JWT validation
    private static final List<String> SKIP_FILTER_URLS = Arrays.asList(
            "/actuator/health", "/actuator/info",
            "/swagger-ui", "/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Skip JWT validation for certain endpoints
            if (shouldSkipFilter(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = extractTokenFromRequest(request);

            if (!StringUtils.hasText(token)) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing or invalid token");
                return;
            }

            // Validate token and set authentication
            if (jwtUtil.validateToken(token)) {
                setAuthentication(request, token);
            } else {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
                return;
            }

        } catch (Exception e) {
            logger.error("Error processing JWT token: ", e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Authentication error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_FILTER_URLS.stream().anyMatch(path::startsWith);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void setAuthentication(HttpServletRequest request, String token) {
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        // Ensure role has ROLE_ prefix for Spring Security
        String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(formattedRole));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Add userId to request header for controllers
        request.setAttribute("userId", userId);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.debug("Successfully authenticated user: " + username + " with role: " + role);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                status.getReasonPhrase(),
                message,
                java.time.Instant.now()
        );

        response.getWriter().write(jsonResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return shouldSkipFilter(request);
    }
}