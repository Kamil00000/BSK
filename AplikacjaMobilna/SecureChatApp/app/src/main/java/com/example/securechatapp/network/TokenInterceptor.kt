package com.example.securechatapp.network

import android.content.Context
import com.example.securechatapp.model.AuthPrefs
import com.example.securechatapp.model.RefreshTokenRequest
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Request

class TokenInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = AuthPrefs(context)
        var request = chain.request()
        val path = request.url.encodedPath

        // Nie dodawaj tokena do żądań logowania i odświeżania
        if (path != "/auth/login" && path != "/auth/refresh") {
            prefs.authToken?.let {
                request = request.newBuilder()
                    .addHeader("Authorization", it)
                    .build()
            }
        }

        val response = chain.proceed(request)

        // Jeśli 401 – spróbuj odświeżyć token
        if (response.code == 401 && prefs.refreshToken != null) {
            response.close()

            // Wysyłamy żądanie odświeżające
            val newTokens = try {
                val refreshClient = Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)

                val refreshResponse = refreshClient
                    .refreshAccessToken(RefreshTokenRequest(prefs.refreshToken!!))
                    .execute()

                if (refreshResponse.isSuccessful) {
                    refreshResponse.body()
                } else null
            } catch (e: Exception) {
                null
            }

            newTokens?.let {
                prefs.authToken = "Bearer ${it.token}"
                prefs.refreshToken = it.refreshToken

                // Ponawiamy żądanie z nowym tokenem
                val newRequest = request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer ${it.token}")
                    .build()

                return chain.proceed(newRequest)
            }
        }

        return response
    }
}
