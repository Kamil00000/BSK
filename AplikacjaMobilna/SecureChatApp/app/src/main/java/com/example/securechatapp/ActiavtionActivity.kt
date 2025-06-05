package com.example.securechatapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.securechatapp.databinding.ActivityActivationBinding
import com.example.securechatapp.model.ActivationRequest
import com.example.securechatapp.network.ApiClient
import kotlinx.coroutines.launch

class ActivationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityActivationBinding
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActivationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        username = intent.getStringExtra("username") ?: ""

        binding.btnActivate.setOnClickListener {
            val code = binding.etCode.text.toString()
            if (code.length == 6) {
                activateAccount(username, code)
            } else {
                binding.tvActivateError.text = "Wprowadź poprawny kod"
                binding.tvActivateError.visibility = View.VISIBLE
            }
        }
    }

    private fun activateAccount(username: String, code: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.activateAccount(ActivationRequest(username, code))
                if (response.isSuccessful) {
                    Toast.makeText(this@ActivationActivity, "Konto aktywowane!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ActivationActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Błąd aktywacji"
                    binding.tvActivateError.text = error
                    binding.tvActivateError.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                binding.tvActivateError.text = "Błąd sieci: ${e.message}"
                binding.tvActivateError.visibility = View.VISIBLE
            }
        }
    }
}
