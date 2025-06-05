package com.example.securechatapp.network

import com.example.securechatapp.model.ActivationRequest
import com.example.securechatapp.model.MessageResponse
import com.example.securechatapp.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("users/register")
    suspend fun register(@Body request: RegisterRequest): Response<MessageResponse>

    @POST("users/activate")
    suspend fun activateAccount(@Body request: ActivationRequest): Response<MessageResponse>


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