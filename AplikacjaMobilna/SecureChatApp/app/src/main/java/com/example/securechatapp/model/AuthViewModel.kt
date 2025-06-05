package com.example.securechatapp.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.securechatapp.network.ApiClient
import com.example.securechatapp.network.LoginRequest

class AuthViewModel : ViewModel() {
    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult
    private val _registerResult = MutableLiveData<Result<String>>()
    val registerResult: LiveData<Result<String>> = _registerResult


    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    _loginResult.value = Result.success(response.body()?.token ?: "")
                } else {
                    _loginResult.value = Result.failure(Exception("Login failed"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }


    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val request = RegisterRequest(username, email, password)
                val response = ApiClient.instance.register(request)
                if (response.isSuccessful) {
                    _registerResult.value = Result.success(response.body()?.message ?: "Registration successful")
                } else {
                    _registerResult.value = Result.failure(Exception("Registration failed"))
                }
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
            }
        }
    }

}