package com.example.securechatapp.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/protected")
    suspend fun getProtectedData(
        @Header("Authorization") token: String
    ): Response<ProtectedResponse>
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val expiresIn: Long
)

data class ProtectedResponse(
    val message: String,
    val data: String
)