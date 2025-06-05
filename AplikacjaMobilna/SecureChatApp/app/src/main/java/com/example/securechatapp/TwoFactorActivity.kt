package com.example.securechatapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.securechatapp.databinding.ActivityTwoFactorBinding
import com.example.securechatapp.model.AuthPrefs
import com.example.securechatapp.model.AuthViewModel
import com.example.securechatapp.network.ApiClient
import com.example.securechatapp.network.TwoFactorRequest

class TwoFactorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTwoFactorBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var authPrefs: AuthPrefs
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTwoFactorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authPrefs = AuthPrefs(this)
        ApiClient.init(applicationContext)
        viewModel.initPrefs(applicationContext)

        username = intent.getStringExtra("username") ?: ""

        binding.btnVerify.setOnClickListener {
            val code = binding.etCode.text.toString()
            if (code.isNotEmpty()) {
                viewModel.verifyTwoFactor(username, code)
            }
        }

        viewModel.verifyTwoFactorResult.observe(this) { result ->
            result.onSuccess {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.onFailure {
                binding.tvError.text = it.message
            }
        }
    }
}
