package com.example.securechatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.securechatapp.databinding.ActivityLoginBinding
import com.example.securechatapp.model.AuthPrefs
import com.example.securechatapp.model.AuthViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var authPrefs: AuthPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authPrefs = AuthPrefs(this)

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
                binding.tvError.text = it.message
            }
        }
    }
}