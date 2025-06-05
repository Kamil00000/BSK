package com.example.securechatapp.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private lateinit var appContext: Context

    fun init(context: Context) {
        this.appContext = context.applicationContext
    }

    val instance: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(appContext))
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

