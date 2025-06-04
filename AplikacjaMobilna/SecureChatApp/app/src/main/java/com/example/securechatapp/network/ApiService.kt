package com.example.securechatapp.network

import com.example.securechatapp.model.ActivationRequest
import com.example.securechatapp.model.RefreshTokenRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refreshAccessTokenSuspend(
        @Body request: RefreshTokenRequest
    ): Response<LoginResponse>

    @POST("auth/refresh")
    fun refreshAccessToken(
        @Body request: RefreshTokenRequest
    ): Call<LoginResponse>

    @POST("/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Void>

    @POST("auth/login/2fa")
    suspend fun loginWith2FA(@Body request: LoginRequest): Response<Void>

    @POST("auth/login/2fa/verify")
    suspend fun verify2FA(@Body request: ActivationRequest): Response<LoginResponse>

}

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val refreshToken: String
)

data class ProtectedResponse(
    val message: String,
    val data: String
)

data class TwoFactorRequest(
    val username: String,
    val password: String,
    val code: String
)