package com.example.securechatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.securechatapp.databinding.ActivityLoginBinding
import com.example.securechatapp.model.AuthPrefs
import com.example.securechatapp.model.AuthViewModel
import com.example.securechatapp.network.ApiClient

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var authPrefs: AuthPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authPrefs = AuthPrefs(this)
        ApiClient.init(applicationContext)
        viewModel.initPrefs(applicationContext)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(username, password)
            }
        }

        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { token ->
                authPrefs.authToken = "Bearer $token"
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.onFailure {
                val message = it.message ?: ""
                if (message.contains("2FA")) {
                    // Przejdź do TwoFactorActivity
                    val intent = Intent(this, TwoFactorActivity::class.java)
                    viewModel.initiateTwoFactor(binding.etUsername.text.toString(), binding.etPassword.text.toString())
                    intent.putExtra("username", binding.etUsername.text.toString())
                    startActivity(intent)
                } else {
                    binding.tvError.text = message
                }
            }
        }

    }
}