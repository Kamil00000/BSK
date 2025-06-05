package com.example.securechatapp.model

 class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String = "ROLE_USER"  // domyślnie zwykły użytkownik
)
