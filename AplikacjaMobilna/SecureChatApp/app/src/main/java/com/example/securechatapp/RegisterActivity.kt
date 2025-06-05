package com.example.securechatapp

import android.content.Intent
import com.example.securechatapp.databinding.ActivityRegisterBinding
import com.example.securechatapp.model.AuthViewModel

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    private var enteredUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                enteredUsername = username
                viewModel.register(username, email, password)
            } else {
                binding.tvError.text = "Wszystkie pola muszą być wypełnione"
            }
        }

        viewModel.registerResult.observe(this) { result ->
            result.onSuccess {
                val intent = Intent(this, ActivationActivity::class.java)
                intent.putExtra("username", enteredUsername)
                startActivity(intent)
                finish()
            }.onFailure {
                binding.tvError.text = it.message ?: "Błąd rejestracji"
            }
        }
    }
}
