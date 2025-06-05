package com.example.securechatapp.model

import android.content.Context
import android.util.Log
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


    lateinit var authPrefs: AuthPrefs

    fun initPrefs(context: Context) {
        authPrefs = AuthPrefs(context)
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.login(LoginRequest(username, password))
                Log.d("Login", "HTTP status: ${response.code()}")

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.d("Login", "Response body: $loginResponse")
                    if (loginResponse != null) {
                        authPrefs.authToken = "Bearer ${loginResponse.token}"
                        authPrefs.refreshToken = loginResponse.refreshToken
                        _loginResult.value = Result.success(loginResponse.token)
                    } else {
                        Log.e("Login", "Empty response body")
                        _loginResult.value = Result.failure(Exception("Empty response"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("Login", "Login failed: $errorBody")
                    _loginResult.value = Result.failure(Exception("Login failed: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("Login", "Exception during login", e)
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

    fun refreshToken() {
        viewModelScope.launch {
            try {
                val token = authPrefs.refreshToken ?: return@launch
                val response = ApiClient.instance.refreshAccessTokenSuspend(RefreshTokenRequest(token))
                if (response.isSuccessful) {
                    val newTokens = response.body()
                    if (newTokens != null) {
                        authPrefs.authToken = "Bearer ${newTokens.token}"
                        authPrefs.refreshToken = newTokens.refreshToken
                    }
                } else {
                    // Można się wylogować albo pokazać komunikat
                }
            } catch (e: Exception) {
                // Loguj błąd
            }
        }
    }

    private val _twoFactorResult = MutableLiveData<Result<Unit>>()
    val twoFactorResult: LiveData<Result<Unit>> = _twoFactorResult

    fun initiateTwoFactor(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.loginWith2FA(LoginRequest(username, password))
                if (response.isSuccessful) {
                    _twoFactorResult.value = Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _twoFactorResult.value = Result.failure(Exception("2FA inicjacja nie powiodła się: $errorBody"))
                }
            } catch (e: Exception) {
                _twoFactorResult.value = Result.failure(e)
            }
        }
    }

    private val _verifyTwoFactorResult = MutableLiveData<Result<String>>()
    val verifyTwoFactorResult: LiveData<Result<String>> = _verifyTwoFactorResult

    fun verifyTwoFactor(username: String, code: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.instance.verify2FA(ActivationRequest(username, code))
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        authPrefs.authToken = "Bearer ${loginResponse.token}"
                        authPrefs.refreshToken = loginResponse.refreshToken
                        _verifyTwoFactorResult.value = Result.success(loginResponse.token)
                    } else {
                        _verifyTwoFactorResult.value = Result.failure(Exception("Pusta odpowiedź"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _verifyTwoFactorResult.value = Result.failure(Exception("Weryfikacja 2FA nie powiodła się: $errorBody"))
                }
            } catch (e: Exception) {
                _verifyTwoFactorResult.value = Result.failure(e)
            }
        }
    }

}
