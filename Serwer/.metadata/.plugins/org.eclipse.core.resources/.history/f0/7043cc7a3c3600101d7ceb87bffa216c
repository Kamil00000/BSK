package com.example.auth_service.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String onlyForAdmin() {
        return "Dostęp tylko dla ADMINA";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String onlyForUser() {
        return "Dostęp tylko dla USERA";
    }

    @GetMapping("/all")
    public String forEveryone() {
        return "Dostęp dla każdego";
    }
}